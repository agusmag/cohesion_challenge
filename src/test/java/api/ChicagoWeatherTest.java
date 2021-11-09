package api;

import helpers.api.ApiHelper;
import io.restassured.response.Response;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.testng.Assert.assertFalse;

public class ChicagoWeatherTest {
    private final String apiUri = "https://data.cityofchicago.org/resource/k7hf-8y75.json";
    private final String userToken = "";
    private final HashMap<String, Object> queryParams = new HashMap<>();

    @BeforeTest()
    public void beforeTest() {
        queryParams.clear();
    }

    /***
     * Story 1: As a user of the API I want to list all measurements taken by the station on Oak Street in json format.
     * • GIVEN BEACH WEATHER STATION SENSOR “OAK STREET”
     * • WHEN THE USER REQUESTS STATION DATA
     * • THEN ALL DATA MEASUREMENTS CORRESPOND TO ONLY THAT STATION
     */
    @Test(priority = 1)
    public void listAllMeasurementsOnOakStreet() {
        // ARRANGE
        queryParams.put("station_name", "Oak Street Weather Station");

        // ACT
        Response result = ApiHelper.get(apiUri, queryParams, "");

        System.out.println(result.getBody().prettyPrint());

        // ASSERT
        result.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("station_name", everyItem(equalTo("Oak Street Weather Station")));
    }

    /***
     * Story 2: As a user of the API I want to be able to page through JSON data sets of 2019 taken by the sensor on 63rd Street.
     * • GIVEN THE BEACH WEATHER STATION ON 63RD STREET’S SENSOR DATA OF 2019
     * • WHEN THE USER REQUESTS DATA FOR THE FIRST 10 MEASUREMENTS
     * • AND THE SECOND PAGE OF 10 MEASUREMENTS
     * • THEN THE RETURNED MEASUREMENTS OF BOTH PAGES SHOULD NOT REPEAT
     */
    @Test(priority = 20)
    public void verifyPageNavigationInDataSets() {
        // ARRANGE
        queryParams.put("station_name", "63rd Street Weather Station");
        queryParams.put("$limit", "10");
        queryParams.put("$offset", "0"); // Starting at element 0 of array
        queryParams.put("$order", "measurement_id");

        HashMap<String, Object> queryParamsPageTwo = new HashMap<>();
        queryParamsPageTwo.put("station_name", "63rd Street Weather Station");
        queryParamsPageTwo.put("$limit", "10");
        queryParamsPageTwo.put("$offset", "10"); // Starting at element 11 of array
        queryParamsPageTwo.put("$order", "measurement_id");

        // ACT
        Response resultPageOne = ApiHelper.get(apiUri, queryParams, "");
        Response resultPageTwo = ApiHelper.get(apiUri, queryParamsPageTwo, "");

        System.out.println(resultPageOne.getBody().prettyPrint());
        System.out.println(resultPageTwo.getBody().prettyPrint());

        // ASSERT
        // I need to make two requests, one for each page.
        // Then, compare lists to verify that any element of the second page is in first page.
        List<String> measurementsPageOne = resultPageOne.then()
                                                            .extract()
                                                            .body()
                                                            .jsonPath()
                                                            .getList("measurement_id", String.class);

        List<String> measurementsPageTwo = resultPageTwo.then()
                                                            .extract()
                                                            .body()
                                                            .jsonPath()
                                                            .getList("measurement_id", String.class);


        assertFalse(measurementsPageOne.stream().anyMatch(measurementsPageTwo::contains));
    }

    /***
     * Story 3: As a user of the API I expect a SoQL query to fail with an error message if I search using a malformed query. Note: This is a negative test. We want to make sure that the API throws an error when expected.
     * • GIVEN ALL BEACH WEATHER STATION SENSOR DATA OF THE STATION ON 63RD STREET
     * • WHEN THE USER REQUESTS SENSOR DATA BY QUERYING BATTERY_LIFE VALUES THAT ARE LESS THAN THE TEXT “FULL” ($WHERE=BATTERY_LIFE < FULL)
     * • THEN AN ERROR CODE “MALFORMED COMPILER” WITH MESSAGE “COULD NOT PARSE SOQL QUERY” IS RETURNED
     */
    @Test(priority = 30)
    public void verifyErrorMessageUsingMalformedQuery() {
        // ARRANGE
        queryParams.put("station_name", "63rd Street Weather Station");
        queryParams.put("$where", "battery_life < full"); // Using malformed SoQL query (compare string with number)

        // ACT
        Response result = ApiHelper.get(apiUri, queryParams, "");

        System.out.println(result.getBody().prettyPrint());

        // ASSERT
        result.then()
                .assertThat()
                .body("code", equalTo("query.compiler.malformed"))
                .and()
                .body("error", equalTo(true))
                .and()
                .body("message", containsStringIgnoringCase("COULD NOT PARSE SOQL QUERY"));
    }
}
