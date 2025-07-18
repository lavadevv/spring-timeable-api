package ext.vnua.tkb_api_lavadev.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ext.vnua.tkb_api_lavadev.config.ApiProperties;
import ext.vnua.tkb_api_lavadev.exception.ApiException;
import ext.vnua.tkb_api_lavadev.exception.JsonParsingException;
import ext.vnua.tkb_api_lavadev.model.dto.LoginResponseToUser;
import ext.vnua.tkb_api_lavadev.model.request.*;
import ext.vnua.tkb_api_lavadev.model.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeableService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ApiProperties apiProperties;

    private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

    /**
     * Authenticates user and returns login response
     * @param loginRequest contains username and password
     * @return Mono of LoginResponseToUser
     */
    public Mono<LoginResponseToUser> login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getUsername());

        MultiValueMap<String, String> formData = buildLoginFormData(loginRequest);

        return webClient.post()
                .uri(apiProperties.getLoginPath())
                .header("Content-Type", CONTENT_TYPE_FORM)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .map(this::mapToLoginResponseToUser)
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_DELAY))
                .doOnSuccess(response -> log.info("Login successful for user: {}", response.getUsername()))
                .doOnError(error -> log.error("Login failed for user: {}", loginRequest.getUsername(), error));
    }

    /**
     * Retrieves list of terms with fallback mechanism
     * @param token authorization token
     * @return Mono of List<GetListTermResponse>
     */
//    @Cacheable(value = "termsList", key = "#token")
    public Mono<List<GetListTermResponse>> getListTerm(String token) {
        log.info("Fetching terms list");

        return callGetTermAPI(token, apiProperties.getGetTermPath())
                .onErrorResume(ex -> {
                    log.warn("Primary term API failed, trying secondary endpoint", ex);
                    return callGetTermAPI(token, apiProperties.getGetTermPathSecond());
                })
                .doOnSuccess(terms -> log.info("Successfully retrieved {} terms", terms.size()))
                .doOnError(error -> log.error("Failed to retrieve terms", error));
    }

    /**
     * Retrieves timeable list with fallback mechanism
     * @param token authorization token
     * @param request contains term code and other parameters
     * @return Mono of Map containing lesson times and timeable list
     */
    public Mono<Map<String, Object>> getTimeableList(String token, GetTimeableListRequest request) {
        log.info("Fetching timeable list for term: {}", request.getTermCode());

        return callGetTimeableListAPI(token, request, apiProperties.getGetListTimeablePath())
                .onErrorResume(ex -> {
                    log.warn("Primary timeable API failed, trying secondary endpoint", ex);
                    return callGetTimeableListAPI(token, request, apiProperties.getGetListTimeablePathSecond());
                })
                .doOnSuccess(result -> log.info("Successfully retrieved timeable list"))
                .doOnError(error -> log.error("Failed to retrieve timeable list for term: {}", request.getTermCode(), error));
    }

    /**
     * Calls term API and parses response
     */
    private Mono<List<GetListTermResponse>> callGetTermAPI(String token, String path) {
        return webClient.post()
                .uri(path)
                .header(AUTHORIZATION_HEADER, token)
                .retrieve()
                .bodyToMono(String.class)
                .handle((jsonString, sink) -> {
                    try {
                        List<GetListTermResponse> terms = parseTermsResponse(jsonString);
                        sink.next(terms);
                    } catch (Exception e) {
                        sink.error(new JsonParsingException("Failed to parse terms response", e));
                    }
                });
    }

    /**
     * Calls timeable list API and parses response
     */
    private Mono<Map<String, Object>> callGetTimeableListAPI(String token, GetTimeableListRequest request, String path) {
        TimeableListRequestBody requestBody = buildTimeableListRequestBody(request);

        return webClient.post()
                .uri(path)
                .header(AUTHORIZATION_HEADER, token)
                .header("Content-Type", CONTENT_TYPE_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .handle((jsonString, sink) -> {
                    try {
                        Map<String, Object> result = parseTimeableResponse(jsonString);
                        sink.next(result);
                    } catch (Exception e) {
                        sink.error(new JsonParsingException("Failed to parse timeable response", e));
                    }
                });
    }

    /**
     * Builds form data for login request
     */
    private MultiValueMap<String, String> buildLoginFormData(LoginRequest loginRequest) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", loginRequest.getUsername());
        formData.add("password", loginRequest.getPassword());
        formData.add("grant_type", GRANT_TYPE_PASSWORD);
        return formData;
    }

    /**
     * Maps LoginResponse to LoginResponseToUser
     */
    private LoginResponseToUser mapToLoginResponseToUser(LoginResponse loginResponse) {
        return LoginResponseToUser.builder()
                .author(apiProperties.getAuthor())
                .message("Authentication successful")
                .token(loginResponse.getAccessToken())
                .username(loginResponse.getUserName())
                .name(loginResponse.getName())
                .email(loginResponse.getPrincipal())
                .role(loginResponse.getRoles())
                .build();
    }

    /**
     * Builds request body for timeable list API
     */
    private TimeableListRequestBody buildTimeableListRequestBody(GetTimeableListRequest request) {
        FilterRequest filterRequest = new FilterRequest(
                Integer.parseInt(request.getTermCode()),
                ""
        );

        PagingRequest paging = new PagingRequest(DEFAULT_PAGE_SIZE, DEFAULT_PAGE_NUMBER);
        OrderingRequest ordering = new OrderingRequest(null, null);
        AdditionalRequest additionalRequest = new AdditionalRequest(paging, List.of(ordering));

        return new TimeableListRequestBody(filterRequest, additionalRequest);
    }

    /**
     * Parses terms response JSON
     */
    private List<GetListTermResponse> parseTermsResponse(String jsonString) throws Exception {
        JsonNode root = objectMapper.readTree(jsonString);
        JsonNode dsHocKyNode = root.path("data").path("ds_hoc_ky");

        List<GetListTermResponse> terms = new ArrayList<>();

        for (JsonNode item : dsHocKyNode) {
            GetListTermResponse term = GetListTermResponse.builder()
                    .termCode(item.path("hoc_ky").asText())
                    .termName(item.path("ten_hoc_ky").asText())
                    .termStartDate(item.path("ngay_bat_dau_hk").asText())
                    .termEndDate(item.path("ngay_ket_thuc_hk").asText())
                    .build();
            terms.add(term);
        }

        return terms;
    }

    /**
     * Parses timeable response JSON
     */
    private Map<String, Object> parseTimeableResponse(String jsonString) throws Exception {
        JsonNode root = objectMapper.readTree(jsonString);

        List<LessonTimeResponse> lessonTimes = parseLessonTimes(root);
        List<WeekInfoResponse> weekInfo = parseWeekInfo(root);

        Map<String, Object> result = new HashMap<>();
        result.put("lessonTimeList", lessonTimes);
        result.put("timeableList", weekInfo);

        return result;
    }

    /**
     * Parses lesson times from JSON
     */
    private List<LessonTimeResponse> parseLessonTimes(JsonNode root) {
        JsonNode listLessonInDayNode = root.path("data").path("ds_tiet_trong_ngay");
        List<LessonTimeResponse> lessonTimes = new ArrayList<>();

        for (JsonNode item : listLessonInDayNode) {
            LessonTimeResponse lessonTime = LessonTimeResponse.builder()
                    .lessonPeriod(item.path("tiet").asInt())
                    .lessonTimeStart(item.path("gio_bat_dau").asText())
                    .lessonTimeEnd(item.path("gio_ket_thuc").asText())
                    .lessonTime(item.path("so_phut").asText())
                    .build();
            lessonTimes.add(lessonTime);
        }

        return lessonTimes;
    }

    /**
     * Parses week info from JSON
     */
    private List<WeekInfoResponse> parseWeekInfo(JsonNode root) {
        JsonNode listTimeableForTheWeekNode = root.path("data").path("ds_tuan_tkb");
        List<WeekInfoResponse> weekInfoList = new ArrayList<>();

        for (JsonNode item : listTimeableForTheWeekNode) {
            WeekInfoResponse weekInfo = WeekInfoResponse.builder()
                    .weekInTerm(item.path("tuan_hoc_ky").asInt())
                    .weekInfo(item.path("thong_tin_tuan").asText())
                    .weekStartDate(item.path("ngay_bat_dau").asText())
                    .weekEndDate(item.path("ngay_ket_thuc").asText())
                    .build();

            List<TimeableListInWeek> timeableList = parseTimeableList(item.path("ds_thoi_khoa_bieu"));
            weekInfo.setTimeableListInWeekList(timeableList);
            weekInfoList.add(weekInfo);
        }

        return weekInfoList;
    }

    /**
     * Parses timeable list from JSON
     */
    private List<TimeableListInWeek> parseTimeableList(JsonNode timeableListNode) {
        List<TimeableListInWeek> timeableList = new ArrayList<>();

        for (JsonNode item : timeableListNode) {
            TimeableListInWeek timeable = TimeableListInWeek.builder()
                    .weekday(item.path("thu_kieu_so").asInt())
                    .lessonPeriodStart(item.path("tiet_bat_dau").asInt())
                    .lessonCount(item.path("so_tiet").asInt())
                    .courseCode(item.path("ma_mon").asText())
                    .courseName(item.path("ten_mon").asText())
                    .credit(item.path("so_tin_chi").asText())
                    .groupCode(item.path("ma_nhom").asText())
                    .groupPracticeCode(item.path("ma_to_th").asText())
                    .combinationCode(item.path("ma_to_hop").asText())
                    .lecturerCode(item.path("ma_giang_vien").asText())
                    .lecturerName(item.path("ten_giang_vien").asText())
                    .classCode(item.path("ma_lop").asText())
                    .className(item.path("ten_lop").asText())
                    .roomCode(item.path("ma_phong").asText())
                    .isMakeupClass(item.path("is_day_bu").asBoolean())
                    .learnDay(item.path("ngay_hoc").asText())
                    .build();
            timeableList.add(timeable);
        }

        return timeableList;
    }
}