package ro.pippo.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import ro.pippo.controller.extractor.Param;
import ro.pippo.core.util.StringUtils;
import ro.pippo.test.PippoRule;
import ro.pippo.test.PippoTest;

/**
 * Test if Locale in request context is used to perform a numeric conversions.
 */
public class ControllerLocaleTest extends PippoTest {

	@ClassRule
	public static PippoRule pippoRule = new PippoRule(new PippoApplication());

	@Test
	public void testLocaleSuccess() {
		Response response = given(Locale.FRENCH, "2 400,50".replaceAll(" ", "\u00A0"), "0,5").get("/sum");
		response.then().statusCode(200);
		assertEquals("2401.00", response.body().asString());
	}

	@Test
	public void testLocaleError() {
		Response response = given(Locale.US, "2 400,50".replaceAll(" ", "\u00A0"), "0,5").get("/sum");
		response.then().statusCode(200);
		assertNotEquals("2401.00", response.body().asString());
	}

	@Path("/")
	public static class SumTwoNumbersController extends Controller {

		@GET("/sum")
		public void sum(@Param("value1") BigDecimal value1, @Param("value2") BigDecimal value2) {
			final BigDecimal result = value1.add(value2);
			log("[ControllerLocaleTest] Raw data from request: value1: {} value2: {}",
					getRequest().getParameter("value1"), getRequest().getParameter("value2"));
			log("[ControllerLocaleTest] Result: {}", result.toString());
			getResponse().send(result.toString());
		}

		private void log(String str, Object... args) {
			System.out.println(StringUtils.format(str, args));
		}

	}

	public static class PippoApplication extends ControllerApplication {

		@Override
		protected void onInit() {
			addControllers(SumTwoNumbersController.class);
		}

	}

	/**
	 * Start building the request part of the test. This request specify
	 * {@code Accept-Language} header and two values for sum.
	 */
	private RequestSpecification given(Locale locale, String value1, String value2) {
		return RestAssured.given().header(new Header("Accept-Language", locale.toLanguageTag()))
				.queryParam("value1", value1).queryParam("value2", value2).when();
	}

}
