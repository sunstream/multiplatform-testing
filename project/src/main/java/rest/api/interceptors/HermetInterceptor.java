package rest.api.interceptors;

import com.google.gson.Gson;
import okhttp3.*;
import okio.Buffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.api.clients.HermetAPIClient;
import rest.api.clients.RetrofitBuilder;
import rest.api.hermet.HermetServiceManager;
import rest.api.payloads.hermet.HermetProxyData;
import rest.api.services.HermetAPI;
import utils.exceptions.FailedConfigurationException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HermetInterceptor implements Interceptor {
	private HermetAPI hermetAPI;
	private final Logger log = LogManager.getLogger();

	@Override
	public Response intercept(Chain chain) throws IOException {
		final Request request = chain.request();
		final Response response;
		if (isAddServiceRequest(request)) {
			if (hermetAPI == null) {
				hermetAPI = new HermetAPIClient().create(HermetAPI.class);
			}
			//check if this service already exists;
			HermetProxyData activeService = getActiveServiceData(request);
			//if not, create and return HermetProxyData
			response = (activeService == null)
					? chain.proceed(request)
					//if it exists, return HermetProxyData with its id
					: buildResponseWithRunningServiceData(request, activeService);
		} else {
			response = chain.proceed(request);
			if (isAddStubRequest(request)) {
				addStubToCreatedStubsList(response);
			}
		}
		return response;
	}

	private boolean isAddServiceRequest(Request request) {
		return request.method().equals("POST") && request.url().encodedPath().endsWith("services");
	}

	private boolean isAddStubRequest(Request request) {
		return request.method().equals("POST") && request.url().encodedPath().endsWith("stubs");
	}

	private HermetProxyData getActiveServiceData(Request newProxyRequest) throws IOException {
		String requestedTargetUrl = getTargetUrlFromAddServiceRequest(newProxyRequest);
		List<HermetProxyData> existingProxies = hermetAPI.getActiveServices().execute().body();
		if (existingProxies == null || existingProxies.isEmpty()) {
			return null;
		} else {
			List<HermetProxyData> proxiesWithTargetUrl = existingProxies.stream()
					.filter(proxy -> proxy.getTargetUrl().equals(requestedTargetUrl))
					.collect(Collectors.toList());
			if (proxiesWithTargetUrl.size() > 1) {
				throw new FailedConfigurationException(
						"More than 1 service exists for target url "+requestedTargetUrl
								+ "\n\tActive services found: "
								+ proxiesWithTargetUrl.stream().map(HermetProxyData::toString)
								.collect(Collectors.joining("\n")));
			}
			return proxiesWithTargetUrl.isEmpty() ? null : proxiesWithTargetUrl.get(0);
		}
	}

	private String getTargetUrlFromAddServiceRequest(Request newProxyRequest) throws IOException {
		final Request copy = newProxyRequest.newBuilder().build();
		final Buffer buffer = new Buffer();
		copy.body().writeTo(buffer);
		HermetProxyData requestedProxy = new Gson().fromJson(buffer.readUtf8(), HermetProxyData.class);
		return requestedProxy.getTargetUrl();
	}

	private Response buildResponseWithRunningServiceData(Request request, HermetProxyData existingProxyData) {
//		String responseString = new Gson().toJson(existingProxyData);
		String locationHeader = String.format("%s/api/services/%s",
				existingProxyData.getProxyHost(), existingProxyData.getId());
		Response.Builder builder = new Response.Builder()
				.code(201)
				.message("Returning existing service data")
				.request(request)
				.protocol(Protocol.HTTP_1_0)
				.body(ResponseBody.create(MediaType.parse("application/json"), ""))
				.addHeader("Location", locationHeader);
		RetrofitBuilder.COMMON_HEADERS.forEach(builder::addHeader);
		return builder.build();
	}

	private void addStubToCreatedStubsList(Response response) throws IOException {
		log.debug("Created a stub. Message: " + response.message());
		if (response.body() != null) {
			log.debug("Body: " + response.body().string());
		}
		log.debug("Headers: " + response.headers().toString());
		HermetServiceManager.addStubFromResponse(response);
	}


}