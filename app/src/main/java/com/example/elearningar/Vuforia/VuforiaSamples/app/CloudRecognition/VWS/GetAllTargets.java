package com.example.elearningar.Vuforia.VuforiaSamples.app.CloudRecognition.VWS;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;


//See the Vuforia Web Services Developer API Specification - https://developer.vuforia.com/resources/dev-guide/listing-targets-cloud-database

public class GetAllTargets {

	//Server Keys
	private String accessKey = "4fe020958934246cb57cced257f207e3a18f3881";
	private String secretKey = "bf515e4687e2029f6825cbc3b6c34140bdc42d40";


	private String url = "https://vws.vuforia.com";

	private void getTargets() throws URISyntaxException, ClientProtocolException, IOException {
		HttpGet getRequest = new HttpGet();
		HttpClient client = new DefaultHttpClient();
		getRequest.setURI(new URI(url + "/targets"));
		setHeaders(getRequest);
		
		HttpResponse response = client.execute(getRequest);
		System.out.println(EntityUtils.toString(response.getEntity()));
	}
	
	private void setHeaders(HttpUriRequest request) {
		SignatureBuilder sb = new SignatureBuilder();
		request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
		request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
	}

	public static void main(String[] args) throws URISyntaxException, ClientProtocolException, IOException {
		GetAllTargets g = new GetAllTargets();
		g.getTargets();
	}
}
