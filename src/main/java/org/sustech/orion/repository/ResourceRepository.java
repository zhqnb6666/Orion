package org.sustech.orion.repository;

import org.sustech.orion.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // 根据课程查询资源
    List<Resource> findByCourse_Id(Long courseId);

    // 根据关联的作业查询资源
    List<Resource> findByAssignment_Id(Long assignmentId);

    // 根据上传者查询资源
    List<Resource> findByUploadedBy_UserId(Long uploadedById);

    // 组合查询：课程 + 类型（例如课件/习题）
    List<Resource> findByCourse_IdAndType(Long courseId, String type);

    // 模糊搜索资源名称
    List<Resource> findByNameContainingIgnoreCase(String keyword);
}