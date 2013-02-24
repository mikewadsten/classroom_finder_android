package com.mikewadsten.test_umnclass;

import android.net.Uri;

public class WebUtil {
    public static class SearchURL {
        private Uri.Builder builder;
        
        public SearchURL(String base) {
            builder = Uri.parse(base).buildUpon();
        }
        
        public SearchURL path(String path) {
            builder.appendPath(path);
            return this;
        }
        
        public SearchURL query(String key, String value) {
            builder.appendQueryParameter(key, value);
            return this;
        }
        
        public String toURL() {
            return builder.build().toString();
        }
        
        /**
         * Alias over toURL for convenience.
         */
        public String toString() {
            return toURL();
        }
    }
}
