package org.tdar.core.service;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.utils.json.ISO8601LocalDateFormat;
import org.tdar.utils.json.LatLongGeoJsonSerializer;
import org.tdar.utils.json.LatitudeLongitudeBoxWrapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public class JacksonUtils {
    static Logger logger = LoggerFactory.getLogger(JacksonUtils.class);

    public static ObjectMapper initializeObjectMapper() {
        logger.trace("initializing object mapper");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("MMMM d, YYYY"));
        mapper.setDateFormat(new ISO8601LocalDateFormat());

        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.registerModules(new JaxbAnnotationModule());
        Hibernate5Module hibernate4Module = new Hibernate5Module();
        hibernate4Module.enable(Hibernate5Module.Feature.FORCE_LAZY_LOADING);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LatitudeLongitudeBoxWrapper.class, new LatLongGeoJsonSerializer());
        mapper.registerModules(hibernate4Module, module);
        mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        return mapper;
    }

    public static ObjectWriter initializeObjectWriter(ObjectMapper mapper, Class<?> view) {
        ObjectWriter objectWriter = mapper.writer();
        if (view != null) {
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            objectWriter = mapper.writerWithView(view);
        }

        if (TdarConfiguration.getInstance().isPrettyPrintJson()) {
            objectWriter = objectWriter.with(new DefaultPrettyPrinter());
        }

        return objectWriter;
    }

}
