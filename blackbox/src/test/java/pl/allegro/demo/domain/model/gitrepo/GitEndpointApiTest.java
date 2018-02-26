package pl.allegro.demo.domain.model.gitrepo;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=9090"})
public class GitEndpointApiTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void should_fetch_repository_details() throws JSONException {
        // given
        String owner = "octocat";
        String repositoryName = "hello-worId";
        String expected = "{\"description\":\"My first repository on GitHub.\"," +
                          "\"fullName\":\"octocat/hello-worId\"," +
                          "\"cloneUrl\":\"https://github.com/octocat/hello-worId.git\"," +
                          "\"stars\":0," +
                          "\"createdAt\":\"2017-12-14T13:49:16\"}";

        // when
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:9090/repositories/{owner}/{repositoryName}",
                                                                          String.class,
                                                                          owner,
                                                                          repositoryName);
        String body = responseEntity.getBody();

        JSONAssert.assertEquals(expected, body,
                                new CustomComparator(
                                        JSONCompareMode.STRICT,
                                        new Customization("stars",
                                                          new RegularExpressionValueMatcher<>("\\d+")),
                                        new Customization("createdAt",
                                                          new RegularExpressionValueMatcher<>("\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d"))));
    }

    @Test
    public void should_not_found_repository_details() throws JSONException {
        // given
        String owner = "1octocat1";
        String repositoryName = "hello-worId";
        String expected = "{\"status\":\"NOT_FOUND\",\"message\":\"NOT_FOUND\",\"traceId\":\"264996b3-bd27-4f19-acb5-ad93fac8cf7f\"}";

        // when
        HttpStatusCodeException exception = null;
        try {
            restTemplate.getForEntity("http://localhost:9090/repositories/{owner}/{repositoryName}", String.class, owner, repositoryName);
        } catch (HttpStatusCodeException e) {
            exception = e;
        }
        Assert.assertNotNull(exception);

        JSONAssert.assertEquals(expected, exception.getResponseBodyAsString(),
                                new CustomComparator(
                                        JSONCompareMode.STRICT,
                                        new Customization("traceId",
                                                          new RegularExpressionValueMatcher<>("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}"))));
    }
}