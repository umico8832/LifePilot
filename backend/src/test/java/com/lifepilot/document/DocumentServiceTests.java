package com.lifepilot.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifepilot.common.BusinessException;
import com.lifepilot.document.dto.CreateDocumentRequest;
import com.lifepilot.document.dto.DocumentResponse;
import com.lifepilot.document.dto.UpdateDocumentRequest;
import com.lifepilot.space.HouseholdService;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTests {

    @Mock
    private DocumentRecordMapper documentMapper;
    @Mock
    private HouseholdService householdService;

    @InjectMocks
    private DocumentService documentService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;
    private static final Long DOC_ID = 700L;

    // --- createDocument ---

    @Test
    void createDocument_success() {
        CreateDocumentRequest request = new CreateDocumentRequest(
                "冰箱保修卡", "warranty", "海尔",
                LocalDate.of(2025, 6, 1), LocalDate.of(2028, 6, 1),
                "文件柜", "{\"model\":\"BCD-200\"}"
        );

        DocumentResponse response = documentService.createDocument(USER_ID, SPACE_ID, request);

        verify(householdService).requireSpaceMembership(USER_ID, SPACE_ID);
        verify(documentMapper).insert((DocumentRecord) any());
        assertNotNull(response);
    }

    @Test
    void createDocument_invalidType_throwsException() {
        CreateDocumentRequest request = new CreateDocumentRequest(
                "文档", "unknown_type", null, null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> documentService.createDocument(USER_ID, SPACE_ID, request));
        verify(documentMapper, never()).insert((DocumentRecord) any());
    }

    @Test
    void createDocument_nonMember_throwsException() {
        doThrow(new BusinessException("FORBIDDEN", "Not a member"))
                .when(householdService).requireSpaceMembership(USER_ID, SPACE_ID);

        CreateDocumentRequest request = new CreateDocumentRequest(
                "发票", "invoice", null, null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> documentService.createDocument(USER_ID, SPACE_ID, request));
    }

    // --- listDocuments ---

    @Test
    void listDocuments_returnsResults() {
        DocumentRecord doc = new DocumentRecord();
        doc.setId(DOC_ID);
        doc.setHouseholdId(SPACE_ID);
        doc.setTitle("保修卡");
        doc.setType("warranty");
        doc.setCreatedAt(LocalDateTime.now());

        when(documentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(doc));

        List<DocumentResponse> result = documentService.listDocuments(USER_ID, SPACE_ID, null);

        assertEquals(1, result.size());
    }

    @Test
    void listDocuments_withTypeFilter() {
        when(documentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<DocumentResponse> result = documentService.listDocuments(USER_ID, SPACE_ID, "invoice");

        assertTrue(result.isEmpty());
    }

    @Test
    void listDocuments_empty_returnsEmptyList() {
        when(documentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<DocumentResponse> result = documentService.listDocuments(USER_ID, SPACE_ID, null);

        assertTrue(result.isEmpty());
    }

    // --- getDocument ---

    @Test
    void getDocument_found() {
        DocumentRecord doc = new DocumentRecord();
        doc.setId(DOC_ID);
        doc.setHouseholdId(SPACE_ID);
        doc.setTitle("保修卡");
        doc.setType("warranty");

        when(documentMapper.selectById(DOC_ID)).thenReturn(doc);

        DocumentResponse result = documentService.getDocument(USER_ID, SPACE_ID, DOC_ID);

        assertNotNull(result);
    }

    @Test
    void getDocument_notFound_throwsException() {
        when(documentMapper.selectById(DOC_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> documentService.getDocument(USER_ID, SPACE_ID, DOC_ID));
    }

    @Test
    void getDocument_wrongSpace_throwsException() {
        DocumentRecord doc = new DocumentRecord();
        doc.setId(DOC_ID);
        doc.setHouseholdId(999L);

        when(documentMapper.selectById(DOC_ID)).thenReturn(doc);

        assertThrows(BusinessException.class,
                () -> documentService.getDocument(USER_ID, SPACE_ID, DOC_ID));
    }

    // --- updateDocument ---

    @Test
    void updateDocument_success() {
        DocumentRecord doc = new DocumentRecord();
        doc.setId(DOC_ID);
        doc.setHouseholdId(SPACE_ID);
        doc.setTitle("旧标题");
        doc.setType("invoice");

        when(documentMapper.selectById(DOC_ID)).thenReturn(doc);

        UpdateDocumentRequest request = new UpdateDocumentRequest(
                "新标题", "receipt", "超市",
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
                "抽屉", "{\"amount\":100}"
        );

        DocumentResponse result = documentService.updateDocument(USER_ID, SPACE_ID, DOC_ID, request);

        assertNotNull(result);
        verify(documentMapper).updateById((DocumentRecord) any());
    }

    @Test
    void updateDocument_invalidType_throwsException() {
        DocumentRecord doc = new DocumentRecord();
        doc.setId(DOC_ID);
        doc.setHouseholdId(SPACE_ID);
        doc.setTitle("文档");
        doc.setType("invoice");

        when(documentMapper.selectById(DOC_ID)).thenReturn(doc);

        UpdateDocumentRequest request = new UpdateDocumentRequest(
                null, "bad_type", null, null, null, null, null
        );

        assertThrows(BusinessException.class,
                () -> documentService.updateDocument(USER_ID, SPACE_ID, DOC_ID, request));
        verify(documentMapper, never()).updateById((DocumentRecord) any());
    }

    @Test
    void updateDocument_notFound_throwsException() {
        when(documentMapper.selectById(DOC_ID)).thenReturn(null);

        UpdateDocumentRequest request = new UpdateDocumentRequest(null, null, null, null, null, null, null);

        assertThrows(BusinessException.class,
                () -> documentService.updateDocument(USER_ID, SPACE_ID, DOC_ID, request));
    }

    // --- deleteDocument ---

    @Test
    void deleteDocument_success() {
        DocumentRecord doc = new DocumentRecord();
        doc.setId(DOC_ID);
        doc.setHouseholdId(SPACE_ID);

        when(documentMapper.selectById(DOC_ID)).thenReturn(doc);

        documentService.deleteDocument(USER_ID, SPACE_ID, DOC_ID);

        verify(documentMapper).deleteById((Long) eq(DOC_ID));
    }

    @Test
    void deleteDocument_notFound_throwsException() {
        when(documentMapper.selectById(DOC_ID)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> documentService.deleteDocument(USER_ID, SPACE_ID, DOC_ID));
        verify(documentMapper, never()).deleteById((Long) any());
    }
}