package io.threatrix.threatmatrix.app;

public class DataUtil {

    private void addWithPrefix(Properties properties, Properties other, String prefix) {
        for (String key : other.stringPropertyNames()) {
            String prefixed = prefix + key;
            properties.setProperty(prefixed, other.getProperty(key));
        }
    }

    private Properties getPropertiesFromApplication(Environment environment) {
        Properties properties = new Properties();
        try {
            String property = environment.getProperty(VCAP_APPLICATION, "{}");
            Map<String, Object> map = this.parser.parseMap(property);
            extractPropertiesFromApplication(properties, map);
        } catch (Exception ex) {
            logger.error("Could not parse VCAP_APPLICATION", ex);
        }
        return properties;
    }

    private Properties getPropertiesFromServices(Environment environment) {
        Properties properties = new Properties();
        try {
            String property = environment.getProperty(VCAP_SERVICES, "{}");
            Map<String, Object> map = this.parser.parseMap(property);
            extractPropertiesFromServices(properties, map);
        }
        catch (Exception ex) {
            logger.error("Could not parse VCAP_SERVICES", ex);
        }
        return properties;
    }

    private void extractPropertiesFromServices(Properties properties,
            Map<String, Object> map) {
        if (map != null) {
            for (Object services : map.values()) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) services;
                for (Object object : list) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> service = (Map<String, Object>) object;
                    String key = (String) service.get("name");
                    if (key == null) {
                        key = (String) service.get("label");
                    }
                    flatten(properties, service, key);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void flatten(Properties properties, Map<String, Object> input, String path) {
        for (Entry<String, Object> entry : input.entrySet()) {
            String key = getFullKey(path, entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) {
                // Need a compound key
                flatten(properties, (Map<String, Object>) value, key);
            }
            else if (value instanceof Collection) {
                // Need a compound key
                Collection<Object> collection = (Collection<Object>) value;
                properties.put(key,
                        StringUtils.collectionToCommaDelimitedString(collection));
                int count = 0;
                for (Object item : collection) {
                    String itemKey = "[" + (count++) + "]";
                    flatten(properties, Collections.singletonMap(itemKey, item), key);
                }
            }
            else if (value instanceof String) {
                properties.put(key, value);
            }
            else if (value instanceof Number) {
                properties.put(key, value.toString());
            }
            else if (value instanceof Boolean) {
                properties.put(key, value.toString());
            }
            else {
                properties.put(key, value == null ? "" : value);
            }
        }
    }

}
