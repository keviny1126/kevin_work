package com.cnlaunch.physics.entity;

import java.util.ArrayList;

import android.text.TextUtils;

public class DPUSoftInfo {
	
	public static String BOOT_VERSION = "bootVersion";
	public static String DOWNLOAD_VERSION = "downloadSersion";
	public static String DIAFNOSE_SOFT_VERSION = "diagnoseSoftVersion";
	public static String PRODUCT_FUNCTION_VERSION = "productFunctionVersion";
	public static String BOOT_103_VERSION = "boot103Version";
	/**Boot版本**/
	private String bootVersion;
	/**Download版本**/
	private String downloadSersion;
	/**诊断软件版本**/
	private String diagnoseSoftVersion;
	/**产品功能软件版本**/
	private String productFunctionVersion;
	/**103芯片版本**/
	private String boot103Version;

	public DPUSoftInfo(){}
	public DPUSoftInfo(ArrayList<String> info){
		if(info != null && info.size() >= 5){
			bootVersion = info.get(0);
			downloadSersion = info.get(1);
			diagnoseSoftVersion = info.get(2);
			productFunctionVersion = info.get(3);
			boot103Version = info.get(4);
		}
		if(info != null && info.size() == 4){
			bootVersion = info.get(0);
			downloadSersion = info.get(1);
			diagnoseSoftVersion = info.get(2);
			productFunctionVersion = info.get(3);
			boot103Version = "";
		}
		if(info != null && info.size() == 3){
			bootVersion = info.get(0);
			downloadSersion = info.get(1);
			diagnoseSoftVersion = info.get(2);
			productFunctionVersion = "";
			boot103Version = "";
		}
		if(info != null && info.size() == 2){
			bootVersion = info.get(0);
			downloadSersion = info.get(1);
			diagnoseSoftVersion = "";
			productFunctionVersion = "";
			boot103Version = "";
		}
		if(info != null && info.size() == 1){
			bootVersion = info.get(0);
			downloadSersion = "";
			diagnoseSoftVersion = "";
			productFunctionVersion = "";
			boot103Version = "";
		}
	}
	public DPUSoftInfo(String[] info){
		if(info != null && info.length >= 5){
			bootVersion = info[0];
			downloadSersion = info[1];
			diagnoseSoftVersion = info[2];
			productFunctionVersion = info[3];
			boot103Version = info[4];
		}
		if(info != null && info.length == 4){
			bootVersion = info[0];
			downloadSersion = info[1];
			diagnoseSoftVersion = info[2];
			productFunctionVersion = info[3];
			boot103Version = "";
		}
		if(info != null && info.length == 3){
			bootVersion = info[0];
			downloadSersion = info[1];
			diagnoseSoftVersion = info[2];
			productFunctionVersion = "";
			boot103Version = "";
		}
		if(info != null && info.length == 2){
			bootVersion = info[0];
			downloadSersion = info[1];
			diagnoseSoftVersion = "";
			productFunctionVersion = "";
			boot103Version = "";
		}
		if(info != null && info.length == 1){
			bootVersion = info[0];
			downloadSersion = "";
			diagnoseSoftVersion = "";
			productFunctionVersion = "";
			boot103Version = "";
		}
	}
	
	public String getBootVersion() {
		return bootVersion;
	}
	public void setBootVersion(String bootVersion) {
		this.bootVersion = bootVersion;
	}
	public String getDownloadSersion() {
		return downloadSersion;
	}
	public void setDownloadSersion(String downloadSersion) {
		this.downloadSersion = downloadSersion;
	}
	public String getDiagnoseSoftVersion() {
		return diagnoseSoftVersion;
	}
	public void setDiagnoseSoftVersion(String diagnoseSoftVersion) {
		this.diagnoseSoftVersion = diagnoseSoftVersion;
	}
	public String getProductFunctionVersion() {
		return productFunctionVersion;
	}
	public void setProductFunctionVersion(String productFunctionVersion) {
		this.productFunctionVersion = productFunctionVersion;
	}

	public String getBoot103Version() {
		return boot103Version;
	}

	public void setBoot103Version(String boot103Version) {
		this.boot103Version = boot103Version;
	}

	public boolean isEmpty(){
		if(TextUtils.isEmpty(bootVersion) && TextUtils.isEmpty(downloadSersion)
				&& TextUtils.isEmpty(diagnoseSoftVersion) && TextUtils.isEmpty(productFunctionVersion)
				&& TextUtils.isEmpty(boot103Version)){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "DPUSoftInfo{" +
				"bootVersion='" + bootVersion + '\'' +
				", downloadSersion='" + downloadSersion + '\'' +
				", diagnoseSoftVersion='" + diagnoseSoftVersion + '\'' +
				", productFunctionVersion='" + productFunctionVersion + '\'' +
				", boot103Version='" + boot103Version + '\'' +
				'}';
	}
}
