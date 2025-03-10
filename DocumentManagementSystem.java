// Simulação de um sistema de gerenciamento de documentos em Java

// Importações necessárias
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

// Classe principal para demonstração
public class DocumentManagementSystem {
    public static void main(String[] args) {
        System.out.println("Iniciando Sistema de Gerenciamento de Documentos");
        
        // Inicializar o sistema
        DocumentService documentService = new DocumentService();
        
        // Criar alguns setores
        Department financeiro = new Department("FIN", "Financeiro");
        Department rh = new Department("RH", "Recursos Humanos");
        Department ti = new Department("TI", "Tecnologia da Informação");
        Department juridico = new Department("JUR", "Jurídico");
        
        documentService.registerDepartment(financeiro);
        documentService.registerDepartment(rh);
        documentService.registerDepartment(ti);
        documentService.registerDepartment(juridico);
        
        // Criar alguns documentos
        Document doc1 = new Document("DOC001", "Relatório Financeiro Q1", "Relatório financeiro do primeiro trimestre", financeiro);
        Document doc2 = new Document("DOC002", "Contratações Pendentes", "Lista de contratações pendentes", rh);
        Document doc3 = new Document("DOC003", "Plano de Infraestrutura", "Plano de atualização da infraestrutura", ti);
        
        documentService.addDocument(doc1);
        documentService.addDocument(doc2);
        documentService.addDocument(doc3);
        
        // Iniciar o agendador de transferências
        AutomaticTransferScheduler scheduler = new AutomaticTransferScheduler(documentService);
        scheduler.start();
        
        // Simular algumas operações
        System.out.println("\n--- Documentos por Setor ---");
        documentService.listDocumentsByDepartment();
        
        System.out.println("\n--- Transferindo DOC001 do Financeiro para o Jurídico ---");
        documentService.transferDocument("DOC001", "FIN", "JUR");
        
        System.out.println("\n--- Documentos por Setor após transferência manual ---");
        documentService.listDocumentsByDepartment();
        
        // Configurar uma regra de transferência automática
        System.out.println("\n--- Configurando transferência automática de RH para TI ---");
        scheduler.scheduleTransfer("DOC002", "RH", "TI", 5);
        
        // Aguardar a transferência automática
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n--- Documentos por Setor após transferência automática ---");
        documentService.listDocumentsByDepartment();
        
        // Parar o agendador
        scheduler.stop();
        System.out.println("\nSistema de Gerenciamento de Documentos finalizado");
    }
}

// Modelo de Documento
class Document {
    private String id;
    private String title;
    private String content;
    private Department currentDepartment;
    private List<TransferRecord> transferHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Document(String id, String title, String content, Department department) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.currentDepartment = department;
        this.transferHistory = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Registrar a criação como primeira transferência
        this.transferHistory.add(new TransferRecord(null, department, "Criação do documento"));
    }
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    public Department getCurrentDepartment() {
        return currentDepartment;
    }
    
    public void setCurrentDepartment(Department department) {
        this.currentDepartment = department;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addTransferRecord(Department source, Department target, String reason) {
        this.transferHistory.add(new TransferRecord(source, target, reason));
    }
    
    public List<TransferRecord> getTransferHistory() {
        return Collections.unmodifiableList(transferHistory);
    }
    
    @Override
    public String toString() {
        return String.format("Documento [%s] %s - Setor Atual: %s", id, title, currentDepartment.getName());
    }
}

// Registro de Transferência
class TransferRecord {
    private Department source;
    private Department target;
    private String reason;
    private LocalDateTime timestamp;
    
    public TransferRecord(Department source, Department target, String reason) {
        this.source = source;
        this.target = target;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String sourceStr = source == null ? "Criação" : source.getCode();
        return String.format("[%s] %s -> %s: %s", 
                timestamp.format(formatter), sourceStr, target.getCode(), reason);
    }
}

// Modelo de Setor
class Department {
    private String code;
    private String name;
    
    public Department(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s", code, name);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Department that = (Department) obj;
        return code.equals(that.code);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}

// Serviço de Documentos
class DocumentService {
    private Map<String, Document> documents;
    private Map<String, Department> departments;
    
    public DocumentService() {
        this.documents = new HashMap<>();
        this.departments = new HashMap<>();
    }
    
    public void registerDepartment(Department department) {
        departments.put(department.getCode(), department);
        System.out.println("Setor registrado: " + department);
    }
    
    public Department getDepartment(String code) {
        return departments.get(code);
    }
    
    public void addDocument(Document document) {
        documents.put(document.getId(), document);
        System.out.println("Documento adicionado: " + document);
    }
    
    public Document getDocument(String id) {
        return documents.get(id);
    }
    
    public boolean transferDocument(String documentId, String sourceDeptCode, String targetDeptCode) {
        Document document = documents.get(documentId);
        Department sourceDept = departments.get(sourceDeptCode);
        Department targetDept = departments.get(targetDeptCode);
        
        if (document == null || sourceDept == null || targetDept == null) {
            System.out.println("Erro: Documento ou setor não encontrado");
            return false;
        }
        
        if (!document.getCurrentDepartment().equals(sourceDept)) {
            System.out.println("Erro: Documento não está no setor de origem especificado");
            return false;
        }
        
        // Registrar a transferência
        document.addTransferRecord(sourceDept, targetDept, "Transferência manual");
        document.setCurrentDepartment(targetDept);
        
        System.out.println("Documento transferido com sucesso: " + document.getId() + 
                " de " + sourceDept.getName() + " para " + targetDept.getName());
        return true;
    }
    
    public void listDocumentsByDepartment() {
        Map<Department, List<Document>> docsByDept = documents.values().stream()
                .collect(Collectors.groupingBy(Document::getCurrentDepartment));
        
        for (Department dept : departments.values()) {
            System.out.println(dept.getName() + ":");
            List<Document> deptDocs = docsByDept.getOrDefault(dept, Collections.emptyList());
            if (deptDocs.isEmpty()) {
                System.out.println("  Nenhum documento");
            } else {
                deptDocs.forEach(doc -> System.out.println("  - " + doc.getTitle() + " (" + doc.getId() + ")"));
            }
        }
    }
    
    public void showDocumentHistory(String documentId) {
        Document document = documents.get(documentId);
        if (document == null) {
            System.out.println("Documento não encontrado: " + documentId);
            return;
        }
        
        System.out.println("Histórico de transferências para " + document.getTitle() + " (" + document.getId() + "):");
        document.getTransferHistory().forEach(System.out::println);
    }
}

// Agendador de Transferências Automáticas
class AutomaticTransferScheduler {
    private final DocumentService documentService;
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks;
    
    public AutomaticTransferScheduler(DocumentService documentService) {
        this.documentService = documentService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.scheduledTasks = new HashMap<>();
    }
    
    public void start() {
        System.out.println("Agendador de transferências automáticas iniciado");
    }
    
    public void stop() {
        scheduler.shutdown();
        System.out.println("Agendador de transferências automáticas parado");
    }
    
    public void scheduleTransfer(String documentId, String sourceDeptCode, String targetDeptCode, int delayInSeconds) {
        String taskId = documentId + "_" + sourceDeptCode + "_" + targetDeptCode;
        
        Runnable task = () -> {
            System.out.println("\n--- Executando transferência automática ---");
            boolean success = documentService.transferDocument(documentId, sourceDeptCode, targetDeptCode);
            if (success) {
                System.out.println("Transferência automática concluída com sucesso");
            } else {
                System.out.println("Falha na transferência automática");
            }
            scheduledTasks.remove(taskId);
        };
        
        ScheduledFuture<?> scheduledTask = scheduler.schedule(task, delayInSeconds, TimeUnit.SECONDS);
        scheduledTasks.put(taskId, scheduledTask);
        
        System.out.println("Transferência automática agendada: Documento " + documentId + 
                " será transferido de " + sourceDeptCode + " para " + targetDeptCode + 
                " em " + delayInSeconds + " segundos");
    }
    
    public void cancelScheduledTransfer(String documentId, String sourceDeptCode, String targetDeptCode) {
        String taskId = documentId + "_" + sourceDeptCode + "_" + targetDeptCode;
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(taskId);
        
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTasks.remove(taskId);
            System.out.println("Transferência automática cancelada: " + taskId);
        } else {
            System.out.println("Nenhuma transferência automática encontrada para: " + taskId);
        }
    }
}