package com.ipd.platform.service;

import com.ipd.platform.entity.DocDocument;
import com.ipd.platform.repository.DocDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文档服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocService {

    private final DocDocumentRepository documentRepository;
    private final AuthService authService;

    public Page<DocDocument> listDocs(String keyword, Long projectId, Integer category, int page, int size) {
        return documentRepository.searchDocs(keyword, projectId, category, false, PageRequest.of(page, size));
    }

    public List<DocDocument> getDocTree(Long projectId, Long parentId) {
        if (parentId != null) {
            return documentRepository.findByParentIdOrderBySortOrderAsc(parentId);
        }
        return documentRepository.findByParentIdIsNullOrderBySortOrderAsc();
    }

    public DocDocument getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
    }

    @Transactional
    public DocDocument create(DocDocument doc) {
        doc.setCreatorId(authService.getCurrentUserId());
        doc.setIsFolder(false);
        return documentRepository.save(doc);
    }

    @Transactional
    public DocDocument createFolder(DocDocument folder) {
        folder.setCreatorId(authService.getCurrentUserId());
        folder.setIsFolder(true);
        return documentRepository.save(folder);
    }

    @Transactional
    public DocDocument update(Long id, DocDocument doc) {
        DocDocument existing = getById(id);
        if (doc.getTitle() != null) existing.setTitle(doc.getTitle());
        if (doc.getContent() != null) existing.setContent(doc.getContent());
        if (doc.getCategory() != null) existing.setCategory(doc.getCategory());
        if (doc.getParentId() != null) existing.setParentId(doc.getParentId());
        if (doc.getSortOrder() != null) existing.setSortOrder(doc.getSortOrder());
        return documentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!documentRepository.existsById(id)) throw new RuntimeException("文档不存在");
        documentRepository.deleteById(id);
    }
}
