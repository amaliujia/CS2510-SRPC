package com.aos.rpc.helperClasses;

public class RequestStatus
{
	private double[] result;
	private long elements1_r, elements1_c;
	private long elements2_r, elements2_c;
	private boolean hasResult, completed;
	private String tag;
	
	public RequestStatus()
	{
		tag = "";
		hasResult = false;
		completed = false;
	}

	public double[] getResult() {
		return result;
	}

	public void setResult(double[] result) {
		this.result = result;
	}

	public long getElements1_r() {
		return elements1_r;
	}

	public void setElements1_r(long elements1_r) {
		this.elements1_r = elements1_r;
	}
	
	public long getElements1_c() {
		return elements1_c;
	}

	public void setElements1_c(long elements1_c) {
		this.elements1_c = elements1_c;
	}
	
	public long getElements2_r() {
		return elements2_r;
	}

	public void setElements2_r(long elements2_r) {
		this.elements2_r = elements2_r;
	}
	
	public long getElements2_c() {
		return elements2_c;
	}

	public void setElements2_c(long elements2_c) {
		this.elements2_c = elements2_c;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isHasResult() {
		return hasResult;
	}

	public void setHasResult(boolean hasResult) {
		this.hasResult = hasResult;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}
