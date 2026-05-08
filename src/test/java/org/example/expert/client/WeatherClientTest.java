package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WeatherClientTest {

    @Mock
    private RestTemplateBuilder builder;
    @Mock
    private RestTemplate restTemplate;

    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        given(builder.build()).willReturn(restTemplate);
        weatherClient = new WeatherClient(builder);
        ReflectionTestUtils.setField(weatherClient, "restTemplate", restTemplate);
    }

    @Test
    void HTTP_상태가_200이_아니면_즉시_ServerException이_발생한다() {
        // given
        ResponseEntity<WeatherDto[]> response = new ResponseEntity<>(new WeatherDto[0], HttpStatus.INTERNAL_SERVER_ERROR);
        given(restTemplate.getForEntity(any(URI.class), any(Class.class))).willReturn(response);

        // when & then
        ServerException exception = assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
        assertEquals("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @Test
    void 응답_본문이_null이면_ServerException이_발생한다() {
        // given
        ResponseEntity<WeatherDto[]> response = new ResponseEntity<>(null, HttpStatus.OK);
        given(restTemplate.getForEntity(any(URI.class), any(Class.class))).willReturn(response);

        // when & then
        ServerException exception = assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
        assertEquals("날씨 데이터가 없습니다.", exception.getMessage());
    }

    @Test
    void 응답_본문이_빈_배열이면_ServerException이_발생한다() {
        // given
        ResponseEntity<WeatherDto[]> response = new ResponseEntity<>(new WeatherDto[0], HttpStatus.OK);
        given(restTemplate.getForEntity(any(URI.class), any(Class.class))).willReturn(response);

        // when & then
        ServerException exception = assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
        assertEquals("날씨 데이터가 없습니다.", exception.getMessage());
    }

    @Test
    void 오늘_날짜에_해당하는_날씨가_없으면_ServerException이_발생한다() {
        // given
        WeatherDto[] data = { new WeatherDto("01-01", "Sunny") };
        ResponseEntity<WeatherDto[]> response = new ResponseEntity<>(data, HttpStatus.OK);
        given(restTemplate.getForEntity(any(URI.class), any(Class.class))).willReturn(response);

        // when & then
        ServerException exception = assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
        assertEquals("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.", exception.getMessage());
    }
}
