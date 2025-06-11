package com.star.ai.test.aimcp.enetity;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class McpToolsTest {
    private String funname;
    private String description;
    private McpSchema.JsonSchema inputSchema;
}
