package zzzank.probejs.lang.schema;

import com.google.gson.JsonObject;
import lombok.val;

public class ArrayElement extends SchemaElement<ArrayElement> {
    private final SchemaElement<?> element;

    public ArrayElement(SchemaElement<?> element) {
        this.element = element;
    }

    @Override
    public String getType() {
        return "array";
    }

    @Override
    protected JsonObject toSchema() {
        val object = new JsonObject();
        object.add("items", element.getSchema());
        return object;
    }
}
