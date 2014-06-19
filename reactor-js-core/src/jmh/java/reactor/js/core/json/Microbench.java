package reactor.js.core.json;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivotal.cf.mobile.ats.json.JsonFunctions;
@State(Scope.Benchmark)
public class Microbench {
    
    private String json = "[{\"date\":\"2014-06-18T15:08:36.024Z\",\"integer\":1,\"float\":1.01,\"boolean\":true,\"obj\":{\"str\":\"foobar\"},\"arr\":[true,false,null,null]}]";
    // Toronto weather, adapted from  http://dd.weatheroffice.ec.gc.ca/citypage_weather/xml/ON/s0000458_e.xml
    private String largeJson="{\"xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\",\"xsi:noNamespaceSchemaLocation\":\"http://dd.weather.gc.ca/citypage_weather/schema/site.xsd\",\"license\":\"http://dd.weather.gc.ca/doc/LICENCE_GENERAL.txt\",\"dateTime\":[{\"name\":\"xmlCreation\",\"zone\":\"UTC\",\"UTCOffset\":\"0\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"18\",\"minute\":\"03\",\"timeStamp\":\"20140619180300\",\"textSummary\":\"Thursday June 19, 2014 at 18:03 UTC\"},{\"name\":\"xmlCreation\",\"zone\":\"EDT\",\"UTCOffset\":\"-4\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"14\",\"minute\":\"03\",\"timeStamp\":\"20140619140300\",\"textSummary\":\"Thursday June 19, 2014 at 14:03 EDT\"}],\"location\":{\"continent\":\"North America\",\"country\":{\"code\":\"ca\",\"value\":\"Canada\"},\"province\":{\"code\":\"on\",\"value\":\"Ontario\"},\"name\":{\"code\":\"s0000458\",\"lat\":\"43.74N\",\"lon\":\"79.37W\",\"value\":\"Toronto\"},\"region\":\"City of Toronto\"},\"warnings\":[],\"currentConditions\":{\"station\":{\"code\":\"yyz\",\"lat\":\"43.68N\",\"lon\":\"79.63W\",\"value\":\"Toronto Pearson Int'l Airport\"},\"dateTime\":[{\"name\":\"observation\",\"zone\":\"UTC\",\"UTCOffset\":\"0\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"18\",\"minute\":\"00\",\"timeStamp\":\"20140619180000\",\"textSummary\":\"Thursday June 19, 2014 at 18:00 UTC\"},{\"name\":\"observation\",\"zone\":\"EDT\",\"UTCOffset\":\"-4\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"14\",\"minute\":\"00\",\"timeStamp\":\"20140619140000\",\"textSummary\":\"Thursday June 19, 2014 at 14:00 EDT\"}],\"condition\":\"Cloudy\",\"iconCode\":{\"format\":\"gif\",\"value\":\"10\"},\"temperature\":{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"22.9\"},\"dewpoint\":{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"10.2\"},\"pressure\":{\"unitType\":\"metric\",\"units\":\"kPa\",\"change\":\"0.04\",\"tendency\":\"falling\",\"value\":\"102.1\"},\"visibility\":{\"unitType\":\"metric\",\"units\":\"km\",\"value\":\"24.1\"},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"44\"},\"wind\":{\"speed\":{\"unitType\":\"metric\",\"units\":\"km/h\",\"value\":\"11\"},\"gust\":{\"unitType\":\"metric\",\"units\":\"km/h\"},\"direction\":\"S\",\"bearing\":{\"units\":\"degrees\",\"value\":\"189.0\"}}},\"forecastGroup\":{\"dateTime\":[{\"name\":\"forecastIssue\",\"zone\":\"UTC\",\"UTCOffset\":\"0\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"15\",\"minute\":\"00\",\"timeStamp\":\"20140619150000\",\"textSummary\":\"Thursday June 19, 2014 at 15:00 UTC\"},{\"name\":\"forecastIssue\",\"zone\":\"EDT\",\"UTCOffset\":\"-4\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"11\",\"minute\":\"00\",\"timeStamp\":\"20140619110000\",\"textSummary\":\"Thursday June 19, 2014 at 11:00 EDT\"}],\"regionalNormals\":{\"textSummary\":\"Low 15. High 25.\",\"temperature\":[{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"25\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"15\"}]},\"forecast\":[{\"period\":{\"textForecastName\":\"Today\",\"value\":\"Thursday\"},\"textSummary\":\"Sunny. Wind northeast 20 km/h. High 24. UV index 9 or very high.\",\"cloudPrecip\":[\"Sunny.\"],\"abbreviatedForecast\":{\"iconCode\":{\"format\":\"gif\",\"value\":\"00\"},\"pop\":{\"units\":\"%\"},\"textSummary\":\"Sunny\"},\"temperatures\":{\"textSummary\":\"High 24.\",\"temperature\":{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"24\"}},\"winds\":{\"textSummary\":\"Wind northeast 20 km/h.\",\"wind\":[{\"index\":\"1\",\"rank\":\"major\",\"speed\":{\"unitType\":\"metric\",\"units\":\"km/h\",\"value\":\"20\"},\"gust\":{\"unitType\":\"metric\",\"units\":\"km/h\",\"value\":\"00\"},\"direction\":\"NE\",\"bearing\":{\"units\":\"degrees\",\"value\":\"04\"}},{\"index\":\"2\",\"rank\":\"major\",\"speed\":{\"unitType\":\"metric\",\"units\":\"km/h\",\"value\":\"10\"},\"gust\":{\"unitType\":\"metric\",\"units\":\"km/h\",\"value\":\"00\"},\"direction\":\"NW\",\"bearing\":{\"units\":\"degrees\",\"value\":\"32\"}}]},\"precipitation\":{\"textSummary\":[],\"precipType\":{\"start\":\"\",\"end\":\"\"}},\"uv\":{\"category\":\"very high\",\"index\":\"9\",\"textSummary\":\"UV index 9 or very high.\"},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"45\"}},{\"period\":{\"textForecastName\":\"Tonight\",\"value\":\"Thursday night\"},\"textSummary\":\"Clear. Low 14.\",\"cloudPrecip\":[\"Clear.\"],\"abbreviatedForecast\":{\"iconCode\":{\"format\":\"gif\",\"value\":\"30\"},\"pop\":{\"units\":\"%\"},\"textSummary\":\"Clear\"},\"temperatures\":{\"textSummary\":\"Low 14.\",\"temperature\":{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"14\"}},\"winds\":[],\"precipitation\":{\"textSummary\":[],\"precipType\":{\"start\":\"\",\"end\":\"\"}},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"65\"}},{\"period\":{\"textForecastName\":\"Friday\",\"value\":\"Friday\"},\"textSummary\":\"Sunny. High 22.\",\"cloudPrecip\":[\"Sunny.\"],\"abbreviatedForecast\":{\"iconCode\":{\"format\":\"gif\",\"value\":\"00\"},\"pop\":{\"units\":\"%\"},\"textSummary\":\"Sunny\"},\"temperatures\":{\"textSummary\":\"High 22.\",\"temperature\":{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"22\"}},\"winds\":[],\"precipitation\":{\"textSummary\":[],\"precipType\":{\"start\":\"\",\"end\":\"\"}},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"65\"}},{\"period\":{\"textForecastName\":\"Saturday\",\"value\":\"Saturday\"},\"textSummary\":\"A mix of sun and cloud. Low 14. High 23.\",\"cloudPrecip\":[\"A mix of sun and cloud.\"],\"abbreviatedForecast\":{\"iconCode\":{\"format\":\"gif\",\"value\":\"02\"},\"pop\":{\"units\":\"%\"},\"textSummary\":\"A mix of sun and cloud\"},\"temperatures\":{\"textSummary\":\"Low 14. High 23.\",\"temperature\":[{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"23\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"14\"}]},\"winds\":[],\"precipitation\":{\"textSummary\":[],\"precipType\":{\"start\":\"\",\"end\":\"\"}},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"60\"}},{\"period\":{\"textForecastName\":\"Sunday\",\"value\":\"Sunday\"},\"textSummary\":\"A mix of sun and cloud. Low 13. High 24.\",\"cloudPrecip\":[\"A mix of sun and cloud.\"],\"abbreviatedForecast\":{\"iconCode\":{\"format\":\"gif\",\"value\":\"02\"},\"pop\":{\"units\":\"%\"},\"textSummary\":\"A mix of sun and cloud\"},\"temperatures\":{\"textSummary\":\"Low 13. High 24.\",\"temperature\":[{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"24\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"13\"}]},\"winds\":[],\"precipitation\":{\"textSummary\":[],\"precipType\":{\"start\":\"\",\"end\":\"\"}},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"55\"}},{\"period\":{\"textForecastName\":\"Monday\",\"value\":\"Monday\"},\"textSummary\":\"A mix of sun and cloud with 30 percent chance of showers. Low 14. High 27.\",\"cloudPrecip\":[\"A mix of sun and cloud with 30 percent chance of showers.\"],\"abbreviatedForecast\":{\"iconCode\":{\"format\":\"gif\",\"value\":\"06\"},\"pop\":{\"units\":\"%\",\"value\":\"30\"},\"textSummary\":\"Chance of showers\"},\"temperatures\":{\"textSummary\":\"Low 14. High 27.\",\"temperature\":[{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"27\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"14\"}]},\"winds\":[],\"precipitation\":{\"textSummary\":[],\"precipType\":{\"start\":\"100\",\"end\":\"124\",\"value\":\"rain\"}},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"75\"}},{\"period\":{\"textForecastName\":\"Tuesday\",\"value\":\"Tuesday\"},\"textSummary\":\"A mix of sun and cloud with 30 percent chance of showers. Low 17. High 23.\",\"cloudPrecip\":[\"A mix of sun and cloud with 30 percent chance of showers.\"],\"abbreviatedForecast\":{\"iconCode\":{\"format\":\"gif\",\"value\":\"06\"},\"pop\":{\"units\":\"%\",\"value\":\"30\"},\"textSummary\":\"Chance of showers\"},\"temperatures\":{\"textSummary\":\"Low 17. High 23.\",\"temperature\":[{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"23\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"17\"}]},\"winds\":[],\"precipitation\":{\"textSummary\":[],\"precipType\":{\"start\":\"124\",\"end\":\"148\",\"value\":\"rain\"}},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"80\"}},{\"period\":{\"textForecastName\":\"Wednesday\",\"value\":\"Wednesday\"},\"textSummary\":\"A mix of sun and cloud with 30 percent chance of showers. Low 15. High 24.\",\"cloudPrecip\":[\"A mix of sun and cloud with 30 percent chance of showers.\"],\"abbreviatedForecast\":{\"iconCode\":{\"format\":\"gif\",\"value\":\"06\"},\"pop\":{\"units\":\"%\",\"value\":\"30\"},\"textSummary\":\"Chance of showers\"},\"temperatures\":{\"textSummary\":\"Low 15. High 24.\",\"temperature\":[{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"24\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"15\"}]},\"winds\":[],\"precipitation\":{\"textSummary\":[],\"precipType\":{\"start\":\"148\",\"end\":\"172\",\"value\":\"rain\"}},\"relativeHumidity\":{\"units\":\"%\",\"value\":\"70\"}}]},\"yesterdayConditions\":{\"temperature\":[{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"25.6\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"17.2\"}],\"precip\":{\"unitType\":\"metric\",\"units\":\"mm\",\"value\":\"0.2\"}},\"riseSet\":{\"disclaimer\":\"The information provided here, for the times of the rise and set of the sun, is an estimate included as a convenience to our clients. Values shown here may differ from the official sunrise/sunset data available from (http://hia-iha.nrc-cnrc.gc.ca/sunrise_e.html)\",\"dateTime\":[{\"name\":\"sunrise\",\"zone\":\"UTC\",\"UTCOffset\":\"0\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"09\",\"minute\":\"35\",\"timeStamp\":\"20140619093500\",\"textSummary\":\"Thursday June 19, 2014 at 09:35 UTC\"},{\"name\":\"sunrise\",\"zone\":\"EDT\",\"UTCOffset\":\"-4\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"05\",\"minute\":\"35\",\"timeStamp\":\"20140619053500\",\"textSummary\":\"Thursday June 19, 2014 at 05:35 EDT\"},{\"name\":\"sunset\",\"zone\":\"UTC\",\"UTCOffset\":\"0\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Friday\",\"value\":\"20\"},\"hour\":\"01\",\"minute\":\"02\",\"timeStamp\":\"20140620010200\",\"textSummary\":\"Friday June 20, 2014 at 01:02 UTC\"},{\"name\":\"sunset\",\"zone\":\"EDT\",\"UTCOffset\":\"-4\",\"year\":\"2014\",\"month\":{\"name\":\"June\",\"value\":\"06\"},\"day\":{\"name\":\"Thursday\",\"value\":\"19\"},\"hour\":\"21\",\"minute\":\"02\",\"timeStamp\":\"20140619210200\",\"textSummary\":\"Thursday June 19, 2014 at 21:02 EDT\"}]},\"almanac\":{\"temperature\":[{\"period\":\"1938-2012\",\"unitType\":\"metric\",\"units\":\"C\",\"year\":\"1995\",\"value\":\"35.5\"},{\"period\":\"1938-2012\",\"unitType\":\"metric\",\"units\":\"C\",\"year\":\"1970\",\"value\":\"4.4\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"24.2\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"12.4\"},{\"unitType\":\"metric\",\"units\":\"C\",\"value\":\"18.3\"}],\"precipitation\":[{\"period\":\"1938-2012\",\"unitType\":\"metric\",\"units\":\"mm\",\"year\":\"1961\",\"value\":\"33.5\"},{\"period\":\"1938-2012\",\"unitType\":\"metric\",\"units\":\"cm\",\"year\":\"1938\",\"value\":\"0.0\"},{\"period\":\"1938-2012\",\"unitType\":\"metric\",\"units\":\"mm\",\"year\":\"1961\",\"value\":\"33.5\"},{\"period\":\"1955-2012\",\"unitType\":\"metric\",\"units\":\"cm\",\"year\":\"1955\",\"value\":\"0.0\"}],\"pop\":{\"units\":\"%\",\"value\":\"37.0\"}}}";
            
    private ObjectMapper mapper = JsonFunctions.getObjectMapper();
    private ScriptEngine scriptEngine;
    
    @Setup
    public void init() throws ScriptException, JsonParseException, JsonMappingException, IOException {
        
        scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
        scriptEngine.put("json", json);
        scriptEngine.put("Object_class", Object.class);
        scriptEngine.put("largeJson", largeJson);
        scriptEngine.put("mapper", mapper);
        scriptEngine.eval("var REACTOR = Java.type('reactor.js.core.json.JsonFunctions');");
        scriptEngine.eval("var JACKSON = Java.type('com.pivotal.cf.mobile.ats.json.JsonFunctions');");
        scriptEngine.eval("var jsObj = [{date: new Date(),integer:1,float:1.01,boolean:true,obj:{str:'foobar'},arr:[true,false,null,undefined]}];");
        scriptEngine.eval("var mecObj = JSON.parse(largeJson)");
    }

    @Benchmark
    public void parseWithNashornJSON() throws Exception {
        scriptEngine.eval("JSON.parse(json);");
    }
    
    @Benchmark
    public void parseWithJacksonNativeJS() throws Exception {
        scriptEngine.eval("JACKSON.parse(json);");
    }
    
    @Benchmark
    public void parseWithReactorJs() throws Exception {
        scriptEngine.eval("REACTOR.parse(json);");
    }
    
    @Benchmark
    public void parseWithNashornJSONLarge() throws Exception {
        scriptEngine.eval("JSON.parse(largeJson);");
    }
    
    @Benchmark
    public void parseWithJacksonNativeJSLarge() throws Exception {
        scriptEngine.eval("JACKSON.parse(largeJson);");
    }
    
    @Benchmark
    public void parseWithReactorJsLarge() throws Exception {
        scriptEngine.eval("REACTOR.parse(largeJson);");
    }

    @Benchmark
    public void parseWithJackson() throws Exception {
        scriptEngine.eval("mapper.readValue(json, Object_class);");
    }
    
    @Benchmark
    public void parseWithJacksonLarge() throws Exception {
        scriptEngine.eval("mapper.readValue(largeJson, Object_class);");
    }
    
    @Benchmark
    public void stringifyWithNashornJSON() throws Exception {
        scriptEngine.eval("JSON.stringify(jsObj);");
    }
    
    @Benchmark
    public void stringifyWithJacksonNativeJs() throws Exception {
        scriptEngine.eval("JACKSON.stringify(jsObj);");
    }
    
    @Benchmark
    public void stringifyWithReactorJs() throws Exception {
        scriptEngine.eval("REACTOR.stringify(jsObj);");
    }
    
    @Benchmark
    public void stringifyWithNashornJSONLarge() throws Exception {
        scriptEngine.eval("JSON.stringify(mecObj);");
    }
    
    @Benchmark
    public void stringifyWithJacksonNativeJsLarge() throws Exception {
        scriptEngine.eval("JACKSON.stringify(mecObj);");
    }
    
    @Benchmark
    public void stringifyWithReactorJsLarge() throws Exception {
        scriptEngine.eval("REACTOR.stringify(mecObj);");
    }
    

}
