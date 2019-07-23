package com.example.elearningar.Vuforia.VuforiaSamples.app.CloudRecognition.VWS;

import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;


// See the Vuforia Web Services Developer API Specification - https://developer.vuforia.com/resources/dev-guide/adding-target-cloud-database-api

public class PostNewTarget implements TargetStatusListener {

    private static final String LOGTAG = "PostNewTarget";

    //Server Keys
	private String accessKey = "4fe020958934246cb57cced257f207e3a18f3881";
	private String secretKey = "bf515e4687e2029f6825cbc3b6c34140bdc42d40";

	private String url = "https://vws.vuforia.com";


	private String targetName = "[ target id in firebase ]";


    private String VideoURL = "[ VideoURL ]";

	private String imageLocation = "[ file system path ]";

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public void setImageLocation(String imageLocation) {
		this.imageLocation = imageLocation;
	}
    public void setVideoURL(String videoURL) {
        VideoURL = videoURL;
    }

	private TargetStatusPoller targetStatusPoller;
	
	private final float pollingIntervalMinutes = 60;//poll at 1-hour interval


    public PostNewTarget() {
    }

    private String postTarget() throws URISyntaxException, ClientProtocolException, IOException, JSONException {



		HttpPost postRequest = new HttpPost();
		HttpClient httpClient = new DefaultHttpClient();
		postRequest.setURI(new URI(url + "/targets"));
		JSONObject requestBody = new JSONObject();
		
		setRequestBody(requestBody);
		postRequest.setEntity(new StringEntity(requestBody.toString()));
		setHeaders(postRequest); // Must be done after setting the body

        Log.i(LOGTAG,  "requestBody" + requestBody.toString() );

        HttpResponse response = httpClient.execute(postRequest);
		String responseBody = EntityUtils.toString(response.getEntity());
		//System.out.println(responseBody);
        Log.i(LOGTAG,  "responseBody" + responseBody );

        JSONObject jobj = new JSONObject(responseBody);
		
		String uniqueTargetId = jobj.has("target_id") ? jobj.getString("target_id") : "";
		//System.out.println("\nCreated target with id: " + uniqueTargetId);
        Log.i(LOGTAG,"\nCreated target with id: " + uniqueTargetId  );

        return uniqueTargetId;
	}
	
	private void setRequestBody(JSONObject requestBody) throws IOException, JSONException {
		File imageFile = new File(imageLocation);
		if(!imageFile.exists()) {
			//System.out.println("File location does not exist!");
            Log.i(LOGTAG, "File location does not exist!" );
            System.exit(1);
		}
		byte[] image = FileUtils.readFileToByteArray(imageFile);
		requestBody.put("name", targetName); // Mandatory
		requestBody.put("width", 320.0); // Mandatory
		requestBody.put("image", Base64.encodeToString(image, Base64.DEFAULT)); // Mandatory
		//requestBody.put("active_flag", 1); // Optional

        JSONObject metadata = new JSONObject();
        metadata.put("url",VideoURL);
		requestBody.put("application_metadata", Base64.encodeToString(metadata.toString().getBytes(), Base64.DEFAULT)); // Optional

	}
	
	private void setHeaders(HttpUriRequest request) {
		SignatureBuilder sb = new SignatureBuilder();
		request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
		request.setHeader(new BasicHeader("Content-Type", "application/json"));
		request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
	}
	
	/**
	 * Posts a new target to the Cloud database; 
	 * then starts a periodic polling until 'status' of created target is reported as 'success'.
	 */
	public void postTargetThenPollStatus() {

        Log.e(LOGTAG , "targetname " + targetName + "  location" + imageLocation );

        String createdTargetId = "";
		try {
			createdTargetId = postTarget();
		} catch (URISyntaxException | IOException | JSONException e) {
			e.printStackTrace();
            Log.e(LOGTAG, "e.printStackTrace()" );

            return;
		}
	
		// Poll the target status until the 'status' is 'success'
		// The TargetState will be passed to the OnTargetStatusUpdate callback 
		if (createdTargetId != null && !createdTargetId.isEmpty()) {
			targetStatusPoller = new TargetStatusPoller(pollingIntervalMinutes, createdTargetId, accessKey, secretKey, this );
			targetStatusPoller.startPolling();
		}
	}
	
	// Called with each update of the target status received by the TargetStatusPoller
	@Override
	public void OnTargetStatusUpdate(TargetState target_state) {
		if (target_state.hasState) {
			
			String status = target_state.getStatus();
			
			//System.out.println("Target status is: " + (status != null ? status : "unknown"));
            Log.i(LOGTAG, "Target status is: " + status != null ? status : "unknown" );

            if (target_state.getActiveFlag() == true && "success".equalsIgnoreCase(status)) {
				
				targetStatusPoller.stopPolling();
				
				//System.out.println("Target is now in 'success' status");
                Log.i(LOGTAG, "Target is now in 'success' status" );

            }
		}
	}
	
}
