package com.example.rmsservices.htmlannotator.service.pojo;

import org.springframework.boot.configurationprocessor.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnnotationDetailsFromJSON {
    
        private Double top;
        private Double left;
        private String value;
        
        private String tag;
        
        private Integer start;
        public Double getTop() {
			return top;
		}
		public void setTop(Double top) {
			this.top = top;
		}
		public Double getLeft() {
			return left;
		}
		public void setLeft(Double left) {
			this.left = left;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}
		public Integer getStart() {
			return start;
		}
		public void setStart(Integer start) {
			this.start = start;
		}
		public Integer getEnd() {
			return end;
		}
		public void setEnd(Integer end) {
			this.end = end;
		}
		public JSONObject getClientRects() {
			return clientRects;
		}
		public void setClientRects(JSONObject clientRects) {
			this.clientRects = clientRects;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		private Integer end;
        
        private JSONObject clientRects;
        private String text;
        
                
}

