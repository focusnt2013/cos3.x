package com.focus.cos.web.ops.vo;

public class ChartDataset
{
	private String id;
	private String title;
	private String ytitle;
	private String subtitle;
	private String timeSeries;
	private String dataSeries;
	private String unit;
	private String tips = "[]";
	
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	public String getYtitle()
	{
		return ytitle;
	}
	public void setYtitle(String ytitle)
	{
		this.ytitle = ytitle;
	}
	public String getSubtitle()
	{
		return subtitle;
	}
	public void setSubtitle(String subtitle)
	{
		this.subtitle = subtitle;
	}
	public String getTimeSeries()
	{
		return timeSeries;
	}
	public void setTimeSeries(String timeSeries)
	{
		this.timeSeries = timeSeries;
	}
	public String getDataSeries()
	{
		return dataSeries;
	}
	public void setDataSeries(String dataSeries)
	{
		this.dataSeries = dataSeries;
	}
	public String getUnit()
	{
		return unit;
	}
	public void setUnit(String unit)
	{
		this.unit = unit;
	}
	public String getTips() {
		return tips;
	}
	public void setTips(String tips) {
		this.tips = tips;
	}
}
