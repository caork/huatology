package com.digitaltwin.backend.config;

import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import static graphql.schema.GraphQLScalarType.newScalar;

@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(createMapScalar());
    }

    private GraphQLScalarType createMapScalar() {
        return newScalar()
                .name("Map")
                .description("A custom scalar for Map type")
                .coercing(new MapCoercing())
                .build();
    }

    private static class MapCoercing implements graphql.schema.Coercing<java.util.Map<String, Object>, java.util.Map<String, Object>> {

        @Override
        public java.util.Map<String, Object> serialize(Object dataFetcherResult) {
            return (java.util.Map<String, Object>) dataFetcherResult;
        }

        @Override
        public java.util.Map<String, Object> parseValue(Object input) {
            return (java.util.Map<String, Object>) input;
        }

        @Override
        public java.util.Map<String, Object> parseLiteral(Object input) {
            return (java.util.Map<String, Object>) input;
        }
    }
}