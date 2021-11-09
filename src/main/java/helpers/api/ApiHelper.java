package helpers.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;

import static io.restassured.RestAssured.given;

/***
 * API Helper
 */
public class ApiHelper {

    public static Response get(String uri, HashMap<String, Object> params, String token) {
        try {
            // Declare query object with request configuration
            RequestSpecification query = given().accept("*/*")
                    .header("Content-Type", "application/json")
//                    .header("X-App-Token", token)
                    .queryParams(params);

            // Request Log
            query.log().all();

            // Make get and return response
            return query.when().get(uri);
        } catch(Exception ex) {
            System.out.println("[ApiHelper/get] Error making request -->" + ex.getMessage());
            throw new NullPointerException();
        }
    }
}
