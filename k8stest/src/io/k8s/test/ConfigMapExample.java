package io.k8s.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.internal.Util;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1ConfigMapList;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
//-Dhttps.protocols=TLSv1.2 -Djavax.net.debug=ssl,handshake to debug hands shake
public class ConfigMapExample {
	
	public static ApiClient client = null ;
	public static void main(String[] args) throws IOException, ApiException {
		ConfigMapExample example = new ConfigMapExample() ;

		client = Config.fromConfig("/Users/tao/.kube/config") ;

//      workaround for TLS protocol not support issue, see https://github.com/kubernetes-client/java/issues/655
		OkHttpClient okclient = client.getHttpClient() ;
		okclient.setConnectionSpecs(Arrays.asList(new ConnectionSpec[]{new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).allEnabledCipherSuites().build()}));
	    Configuration.setDefaultApiClient(client);
	    System.out.println("get all pods");
	    example.getAllPods("mt") ;
	    System.out.println("get all configMap");
	    example.getAllConfigMap("mt");
//	    System.out.println("Watch ConfigMap");
//	    example.watchConfigMap("mt","config-uc") ;
//	    
//	    System.out.println("Modify ConfigMap");
//	    example.patchConfigMap("mt","config-uc");
	  }
	
	
	  private void getAllPods(String ns) throws ApiException{
		  
		    CoreV1Api api = new CoreV1Api(); 
		    V1PodList list = api.listNamespacedPod(ns, null, null, null, null, null, null, null, null, null);
		    for (V1Pod item : list.getItems()) {
		    	if(ns != null) {
		    		if(ns.equalsIgnoreCase(item.getMetadata().getNamespace())) {
		    			System.out.println("Pod: "+ item.getMetadata().getName());
		    		}
		    		
		    	}else {
		    		 System.out.println("Pod: "+item.getMetadata().getName());
		    	}
		     
		    }
	  }
	  
	  private void getAllConfigMap(String ns) throws ApiException {
		  CoreV1Api api = new CoreV1Api(); 
		  V1ConfigMapList list = api.listNamespacedConfigMap(ns, null, null, null, null, null, null, null, null, null);

		  for(V1ConfigMap cMap:list.getItems()) {
			  if(ns != null) {
		    		if(ns.equalsIgnoreCase(cMap.getMetadata().getNamespace())) {
		    			System.out.println("ConfigMap: "+cMap.getMetadata().getName());
		    		}
		    		
		    	}else {
		    		 System.out.println("ConfigMap: "+cMap.getMetadata().getName());
		    	}
		  }
	  }
	  private void patchConfigMap(String ns,String mapName) throws ApiException {
		  CoreV1Api api = new CoreV1Api(); 
		  /**
		   * JSON merge supports remove, add and replace option
		   * 
		   * 
			  //verfy using: kubectl patch configmap/config-uc -n mt --type merge -p '{"data":{"profile_test6":""}}'
			  //--type json/merge/strategic
			  //http://erosb.github.io/post/json-patch-vs-merge-patch/  
			  //--type json 
			  //kubectl patch configmap/config-uc -n mt --type json -p '[{"op":"replace","path":"/data/profile_test6","value":"sample profile1"}]'
			  //kubectl patch configmap/config-uc -n mt --type json -p '[{"op":"add","path":"/data/profile_test6","value":"sample profile"}]'
			  //kubectl patch configmap/config-uc -n mt --type json -p '[{"op":"remove","path":"/data/profile_test6"}]'
			  
		   * */
		  String patchBody0  =  "{\"op\":\"remove\",\"path\":\"/data/profile_test2\"}";
		  String patchBody1 =  "{\"op\":\"add\",\"path\":\"/data/profile_test2\",\"value\":\"this is a test profile\"}";
		  String patchBody2 =  "{\"op\":\"replace\",\"path\":\"/data/profile_test2\",\"value\":\"this is a test profile--test\"}";
		  ArrayList<JsonObject> arr = new ArrayList<>();
		  Object obj = ((JsonElement)(new Gson()).fromJson(patchBody0,  JsonElement.class)).getAsJsonObject();
		  arr.add((JsonObject) obj) ;
		  
		  obj = ((JsonElement)(new Gson()).fromJson(patchBody1,  JsonElement.class)).getAsJsonObject();
		  arr.add((JsonObject) obj) ;
		  
		  obj = ((JsonElement)(new Gson()).fromJson(patchBody2,  JsonElement.class)).getAsJsonObject();
		  arr.add((JsonObject) obj) ;
		  api.patchNamespacedConfigMap("config-uc", "mt",arr, null, null) ;


	  }
	  private void watchConfigMap(String ns,String mapName) throws ApiException, IOException {
		  CoreV1Api api = new CoreV1Api(); 
		  String label = "chart=ibm-unified-console-1.0.0, component=uc, heritage=Tiller, release=multi" ;
		  api.getApiClient().getHttpClient().setReadTimeout(0, TimeUnit.SECONDS);

			  
		  final Watch<V1ConfigMap> watch = Watch.createWatch(client
				  , api.listNamespacedConfigMapCall(ns, null, null, null, null, label, null, null, 0, Boolean.TRUE, null, null)
				  , new TypeToken<Watch.Response<V1ConfigMap>>() {}.getType()) ;

		  for(Watch.Response<V1ConfigMap> item: watch) {
			  System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
		  
		  }
		 System.out.println("here");		  
	  }
}
