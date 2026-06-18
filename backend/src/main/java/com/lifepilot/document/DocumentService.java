package com.lifepilot.document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.document.dto.CreateDocumentRequest;
import com.lifepilot.document.dto.DocumentResponse;
import com.lifepilot.document.dto.UpdateDocumentRequest;
import com.lifepilot.space.HouseholdService;

@Service
public class DocumentService {

    private static final List<String> VALID_TYPES = List.of(
            "invoice", "receipt", "warranty", "contract", "manual", "certificate", "other"
    );

    private final DocumentRecordMapper documentMapper;
    private final HouseholdService householdService;

    public DocumentService(DocumentRecordMapper documentMapper, HouseholdService householdService) {
        this.documentMapper = documentMapper;
        this.householdService = householdService;
    }

    @Transactional
    public DocumentResponse createDocument(Long userId, Long spaceId, CreateDocumentRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        if (!VALID_TYPES.contains(request.type())) {
            throw new BusinessException("INVALID_TYPE", "Type must be one of: " + VALID_TYPES);
        }

        DocumentRecord doc = new DocumentRecord();
        doc.setHouseholdId(spaceId);
        doc.setTitle(request.title());
        doc.setType(request.type());
        doc.setIssuer(request.issuer());
        doc.setDocumentDate(request.documentDate());
        doc.setExpireAt(request.expireAt());
        doc.setStorageLocation(request.storageLocation());
        doc.setMetadataJson(request.metadataJson());

        LocalDateTime now = LocalDateTime.now();
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);

        documentMapper.insert(doc);
        return DocumentResponse.from(doc);
    }

    public List<DocumentResponse> listDocuments(Long userId, Long spaceId, String type) {
        householdService.requireSpaceMembership(userId, spaceId);

        LambdaQueryWrapper<DocumentRecord> wrapper = new LambdaQueryWrapper<DocumentRecord>()
                .eq(DocumentRecord::getHouseholdId, spaceId);

        if (type != null && !type.isBlank()) {
            wrapper.eq(DocumentRecord::getType, type);
        }

        wrapper.orderByDesc(DocumentRecord::getCreatedAt);

        List<DocumentRecord> docs = documentMapper.selectList(wrapper);
        return docs.stream().map(DocumentResponse::from).collect(Collectors.toList());
    }

    public DocumentResponse getDocument(Long userId, Long spaceId, Long docId) {
        householdService.requireSpaceMembership(userId, spaceId);

        DocumentRecord doc = documentMapper.selectById(docId);
        if (doc == null || !doc.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Document not found");
        }

        return DocumentResponse.from(doc);
    }

    @Transactional
    public DocumentResponse updateDocument(Long userId, Long spaceId, Long docId, UpdateDocumentRequest request) {
        householdService.requireSpaceMembership(userId, spaceId);

        DocumentRecord doc = documentMapper.selectById(docId);
        if (doc == null || !doc.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Document not found");
        }

        if (request.title() != null) doc.setTitle(request.title());
        if (request.type() != null) {
            if (!VALID_TYPES.contains(request.type())) {
                throw new BusinessException("INVALID_TYPE", "Type must be one of: " + VALID_TYPES);
            }
            doc.setType(request.type());
        }
        if (request.issuer() != null) doc.setIssuer(request.issuer());
        if (request.documentDate() != null) doc.setDocumentDate(request.documentDate());
        if (request.expireAt() != null) doc.setExpireAt(request.expireAt());
        if (request.storageLocation() != null) doc.setStorageLocation(request.storageLocation());
        if (request.metadataJson() != null) doc.setMetadataJson(request.metadataJson());
        doc.setUpdatedAt(LocalDateTime.now());

        documentMapper.updateById(doc);
        return DocumentResponse.from(doc);
    }

    @Transactional
    public void deleteDocument(Long userId, Long spaceId, Long docId) {
        householdService.requireSpaceMembership(userId, spaceId);

        DocumentRecord doc = documentMapper.selectById(docId);
        if (doc == null || !doc.getHouseholdId().equals(spaceId)) {
            throw new BusinessException("NOT_FOUND", "Document not found");
        }

        documentMapper.deleteById(docId);
    }
}