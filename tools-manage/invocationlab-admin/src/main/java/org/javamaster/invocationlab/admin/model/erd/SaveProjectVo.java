package org.javamaster.invocationlab.admin.model.erd;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class SaveProjectVo {
    private ErdOnlineModel erdOnlineModel;
    private JsonNode delta;
}
