package org.mitre.opensextant.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.mitre.opensextant.placedata.Place;

public class GeoTweetWritable implements Writable {

	public static enum Type {
		USER, BODY
	}
	
	private Date timestamp;
	private String author;
	private String tweet;
	private Double latitude;
	private Double longitude;
	private String matchText;
	private Type type;

	public GeoTweetWritable() {
		super();
	}
	
	
	public GeoTweetWritable(Type type, String author, Date timestamp, String tweet, Double latitude, Double longitude, String matchText) {
		super();
		this.timestamp = timestamp;
		this.author = author;
		this.tweet = tweet;
		this.latitude = latitude;
		this.longitude = longitude;
		this.matchText = (matchText != null) ? matchText : " ";
		this.type = type;
	}


	public GeoTweetWritable(Type type, String author, Date timestamp, String tweet, String matchText, Place place) {
		this(type, author, timestamp, tweet, place.getGeocoord().getLatitude(),  place.getGeocoord().getLongitude(), matchText);
	}
	
	public GeoTweetWritable clone() {
		return new GeoTweetWritable(type, author, timestamp, tweet, latitude, longitude, matchText);
	}

	@Override
	public void readFields(DataInput input) throws IOException {
		this.type = Type.valueOf(input.readUTF());
		this.author = input.readUTF();
		this.timestamp = new Date(input.readLong());
		this.tweet = input.readUTF();
		this.latitude = input.readDouble();
		this.longitude = input.readDouble();
		this.matchText = input.readUTF();
	}

	@Override
	public void write(DataOutput output) throws IOException {
		output.writeUTF(type.name());
		output.writeUTF(author);
		output.writeLong(timestamp.getTime());
		output.writeUTF(tweet);
		output.writeDouble(latitude);
		output.writeDouble(longitude);
		output.writeUTF(matchText);
	}

	public Text toText() {
		return new Text(this.toString());
	}
	
	@Override
	public String toString() {
		return "[type= " + type + ", ts=" + timestamp.getTime() + ", a=" + author + ", lat=" + latitude
				+ ", lon=" + longitude + ", matchText=" + matchText + ", t=" + tweet + "]";
	}

	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getTweet() {
		return tweet;
	}
	public void setTweet(String tweet) {
		this.tweet = tweet;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public String getMatchText() {
		return matchText;
	}
	public void setMatchText(String matchText) {
		this.matchText = matchText;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + ((matchText == null) ? 0 : matchText.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((tweet == null) ? 0 : tweet.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeoTweetWritable other = (GeoTweetWritable) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (matchText == null) {
			if (other.matchText != null)
				return false;
		} else if (!matchText.equals(other.matchText))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (tweet == null) {
			if (other.tweet != null)
				return false;
		} else if (!tweet.equals(other.tweet))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
