package org.sustech.orion.dto;

import lombok.Data;

import java.util.List;

/**
 * 资源数据传输对象
 */
@Data
public class ResourceDTO {
    
    private String title;
    private String description;
    private String type;
    
    // 可以根据需要添加更多字段

} 