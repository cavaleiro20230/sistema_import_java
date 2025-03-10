import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DocumentManagementSystemUI {
    // Componentes principais da aplicação
    private JFrame mainFrame;
    private JTabbedPane tabbedPane;
    private DocumentService documentService;
    private AutomaticTransferScheduler scheduler;
    
    // Painéis para cada funcionalidade
    private JPanel dashboardPanel;
    private JPanel documentsPanel;
    private JPanel departmentsPanel;
    private JPanel transfersPanel;
    private JPanel historyPanel;
    
    // Componentes para gerenciamento de documentos
    private JTable documentsTable;
    private DefaultTableModel documentsTableModel;
    
    // Componentes para gerenciamento de departamentos
    private JTable departmentsTable;
    private DefaultTableModel departmentsTableModel;
    
    // Componentes para transferências
    private JComboBox<String> documentComboBox;
    private JComboBox<String> sourceDeptComboBox;
    private JComboBox<String> targetDeptComboBox;
    private JSpinner delaySpinner;
    
    // Componentes para histórico
    private JComboBox<String> historyDocumentComboBox;
    private JTextArea historyTextArea;
    
    public DocumentManagementSystemUI() {
        // Inicializar o serviço de documentos e o agendador
        documentService = new DocumentService();
        scheduler = new AutomaticTransferScheduler(documentService);
        
        // Configurar a interface gráfica
        setupUI();
        
        // Inicializar com alguns dados de exemplo
        initializeExampleData();
        
        // Iniciar o agendador
        scheduler.start();
    }
    
    private void setupUI() {
        // Configurar o frame principal
        mainFrame = new JFrame("Sistema de Gerenciamento de Documentos");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);
        
        // Criar o painel com abas
        tabbedPane = new JTabbedPane();
        
        // Configurar cada painel
        setupDashboardPanel();
        setupDocumentsPanel();
        setupDepartmentsPanel();
        setupTransfersPanel();
        setupHistoryPanel();
        
        // Adicionar os painéis ao tabbedPane
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "Visão geral do sistema");
        tabbedPane.addTab("Documentos", new ImageIcon(), documentsPanel, "Gerenciar documentos");
        tabbedPane.addTab("Setores", new ImageIcon(), departmentsPanel, "Gerenciar setores");
        tabbedPane.addTab("Transferências", new ImageIcon(), transfersPanel, "Configurar transferências");
        tabbedPane.addTab("Histórico", new ImageIcon(), historyPanel, "Visualizar histórico de transferências");
        
        // Adicionar o tabbedPane ao frame
        mainFrame.add(tabbedPane);
        
        // Exibir o frame
        mainFrame.setVisible(true);
    }
    
    private void setupDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior com informações gerais
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        infoPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // Card para total de documentos
        JPanel docsCard = createInfoCard("Total de Documentos", "0");
        
        // Card para total de setores
        JPanel deptsCard = createInfoCard("Total de Setores", "0");
        
        // Card para transferências pendentes
        JPanel transfersCard = createInfoCard("Transferências Pendentes", "0");
        
        infoPanel.add(docsCard);
        infoPanel.add(deptsCard);
        infoPanel.add(transfersCard);
        
        // Painel central com atividades recentes
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBorder(BorderFactory.createTitledBorder("Atividades Recentes"));
        
        JTextArea activityTextArea = new JTextArea();
        activityTextArea.setEditable(false);
        JScrollPane activityScrollPane = new JScrollPane(activityTextArea);
        activityPanel.add(activityScrollPane, BorderLayout.CENTER);
        
        // Adicionar os painéis ao dashboard
        dashboardPanel.add(infoPanel, BorderLayout.NORTH);
        dashboardPanel.add(activityPanel, BorderLayout.CENTER);
        
        // Painel inferior com ações rápidas
        JPanel quickActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Ações Rápidas"));
        
        JButton newDocButton = new JButton("Novo Documento");
        JButton newDeptButton = new JButton("Novo Setor");
        JButton newTransferButton = new JButton("Nova Transferência");
        
        quickActionsPanel.add(newDocButton);
        quickActionsPanel.add(newDeptButton);
        quickActionsPanel.add(newTransferButton);
        
        dashboardPanel.add(quickActionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createInfoCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void setupDocumentsPanel() {
        documentsPanel = new JPanel(new BorderLayout());
        documentsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior com formulário para adicionar documentos
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Adicionar Novo Documento"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ID do documento
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        JTextField idField = new JTextField(10);
        formPanel.add(idField, gbc);
        
        // Título do documento
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Título:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        JTextField titleField = new JTextField(30);
        formPanel.add(titleField, gbc);
        
        // Conteúdo do documento
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Conteúdo:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        JTextField contentField = new JTextField(30);
        formPanel.add(contentField, gbc);
        
        // Setor do documento
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Setor:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        JComboBox<String> deptComboBox = new JComboBox<>();
        formPanel.add(deptComboBox, gbc);
        
        // Botão para adicionar
        gbc.gridx = 2;
        gbc.gridy = 3;
        JButton addButton = new JButton("Adicionar");
        formPanel.add(addButton, gbc);
        
        // Tabela de documentos
        String[] columnNames = {"ID", "Título", "Setor Atual", "Data de Criação"};
        documentsTableModel = new DefaultTableModel(columnNames, 0);
        documentsTable = new JTable(documentsTableModel);
        JScrollPane tableScrollPane = new JScrollPane(documentsTable);
        
        // Painel de botões para ações na tabela
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editButton = new JButton("Editar");
        JButton deleteButton = new JButton("Excluir");
        JButton viewButton = new JButton("Visualizar");
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);
        
        // Adicionar componentes ao painel de documentos
        documentsPanel.add(formPanel, BorderLayout.NORTH);
        documentsPanel.add(tableScrollPane, BorderLayout.CENTER);
        documentsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupDepartmentsPanel() {
        departmentsPanel = new JPanel(new BorderLayout());
        departmentsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior com formulário para adicionar setores
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Adicionar Novo Setor"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Código do setor
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Código:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        JTextField codeField = new JTextField(10);
        formPanel.add(codeField, gbc);
        
        // Nome do setor
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Nome:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        JTextField nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        
        // Botão para adicionar
        gbc.gridx = 2;
        gbc.gridy = 1;
        JButton addButton = new JButton("Adicionar");
        formPanel.add(addButton, gbc);
        
        // Tabela de setores
        String[] columnNames = {"Código", "Nome", "Qtd. Documentos"};
        departmentsTableModel = new DefaultTableModel(columnNames, 0);
        departmentsTable = new JTable(departmentsTableModel);
        JScrollPane tableScrollPane = new JScrollPane(departmentsTable);
        
        // Painel de botões para ações na tabela
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editButton = new JButton("Editar");
        JButton deleteButton = new JButton("Excluir");
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // Adicionar componentes ao painel de setores
        departmentsPanel.add(formPanel, BorderLayout.NORTH);
        departmentsPanel.add(tableScrollPane, BorderLayout.CENTER);
        departmentsPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupTransfersPanel() {
        transfersPanel = new JPanel(new BorderLayout());
        transfersPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel para transferência manual
        JPanel manualPanel = new JPanel(new GridBagLayout());
        manualPanel.setBorder(BorderFactory.createTitledBorder("Transferência Manual"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Documento
        gbc.gridx = 0;
        gbc.gridy = 0;
        manualPanel.add(new JLabel("Documento:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        documentComboBox = new JComboBox<>();
        manualPanel.add(documentComboBox, gbc);
        
        // Setor de origem
        gbc.gridx = 0;
        gbc.gridy = 1;
        manualPanel.add(new JLabel("De:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        sourceDeptComboBox = new JComboBox<>();
        manualPanel.add(sourceDeptComboBox, gbc);
        
        // Setor de destino
        gbc.gridx = 0;
        gbc.gridy = 2;
        manualPanel.add(new JLabel("Para:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        targetDeptComboBox = new JComboBox<>();
        manualPanel.add(targetDeptComboBox, gbc);
        
        // Botão para transferir
        gbc.gridx = 1;
        gbc.gridy = 3;
        JButton transferButton = new JButton("Transferir Agora");
        manualPanel.add(transferButton, gbc);
        
        // Painel para transferência automática
        JPanel autoPanel = new JPanel(new GridBagLayout());
        autoPanel.setBorder(BorderFactory.createTitledBorder("Transferência Automática"));
        
        // Reutilizar os mesmos componentes de seleção
        gbc.gridx = 0;
        gbc.gridy = 0;
        autoPanel.add(new JLabel("Documento:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        JComboBox<String> autoDocumentComboBox = new JComboBox<>();
        autoPanel.add(autoDocumentComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        autoPanel.add(new JLabel("De:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        JComboBox<String> autoSourceDeptComboBox = new JComboBox<>();
        autoPanel.add(autoSourceDeptComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        autoPanel.add(new JLabel("Para:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        JComboBox<String> autoTargetDeptComboBox = new JComboBox<>();
        autoPanel.add(autoTargetDeptComboBox, gbc);
        
        // Atraso para transferência automática
        gbc.gridx = 0;
        gbc.gridy = 3;
        autoPanel.add(new JLabel("Atraso (segundos):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        delaySpinner = new JSpinner(new SpinnerNumberModel(5, 1, 3600, 1));
        autoPanel.add(delaySpinner, gbc);
        
        // Botão para agendar
        gbc.gridx = 1;
        gbc.gridy = 4;
        JButton scheduleButton = new JButton("Agendar Transferência");
        autoPanel.add(scheduleButton, gbc);
        
        // Painel para transferências agendadas
        JPanel scheduledPanel = new JPanel(new BorderLayout());
        scheduledPanel.setBorder(BorderFactory.createTitledBorder("Transferências Agendadas"));
        
        String[] columnNames = {"Documento", "De", "Para", "Tempo Restante"};
        DefaultTableModel scheduledTableModel = new DefaultTableModel(columnNames, 0);
        JTable scheduledTable = new JTable(scheduledTableModel);
        JScrollPane scheduledScrollPane = new JScrollPane(scheduledTable);
        
        JButton cancelButton = new JButton("Cancelar Transferência");
        JPanel scheduledButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scheduledButtonPanel.add(cancelButton);
        
        scheduledPanel.add(scheduledScrollPane, BorderLayout.CENTER);
        scheduledPanel.add(scheduledButtonPanel, BorderLayout.SOUTH);
        
        // Organizar os painéis
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        topPanel.add(manualPanel);
        topPanel.add(autoPanel);
        
        transfersPanel.add(topPanel, BorderLayout.NORTH);
        transfersPanel.add(scheduledPanel, BorderLayout.CENTER);
    }
    
    private void setupHistoryPanel() {
        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Painel superior para seleção de documento
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Selecione o documento:"));
        
        historyDocumentComboBox = new JComboBox<>();
        selectionPanel.add(historyDocumentComboBox);
        
        JButton viewButton = new JButton("Visualizar Histórico");
        selectionPanel.add(viewButton);
        
        // Área de texto para exibir o histórico
        historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);
        JScrollPane historyScrollPane = new JScrollPane(historyTextArea);
        
        // Adicionar componentes ao painel de histórico
        historyPanel.add(selectionPanel, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
    }
    
    private void initializeExampleData() {
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
        
        // Atualizar as tabelas e comboboxes
        updateDocumentsTable();
        updateDepartmentsTable();
        updateComboBoxes();
    }
    
    private void updateDocumentsTable() {
        // Limpar a tabela
        documentsTableModel.setRowCount(0);
        
        // Adicionar os documentos à tabela
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Document doc : documentService.getAllDocuments()) {
            Object[] row = {
                doc.getId(),
                doc.getTitle(),
                doc.getCurrentDepartment().getName(),
                doc.getCreatedAt().format(formatter)
            };
            documentsTableModel.addRow(row);
        }
    }
    
    private void updateDepartmentsTable() {
        // Limpar a tabela
        departmentsTableModel.setRowCount(0);
        
        // Adicionar os setores à tabela
        for (Department dept : documentService.getAllDepartments()) {
            int docCount = documentService.getDocumentCountByDepartment(dept);
            Object[] row = {
                dept.getCode(),
                dept.getName(),
                docCount
            };
            departmentsTableModel.addRow(row);
        }
    }
    
    private void updateComboBoxes() {
        // Atualizar comboboxes de documentos
        documentComboBox.removeAllItems();
        historyDocumentComboBox.removeAllItems();
        
        for (Document doc : documentService.getAllDocuments()) {
            String item = doc.getId() + " - " + doc.getTitle();
            documentComboBox.addItem(item);
            historyDocumentComboBox.addItem(item);
        }
        
        // Atualizar comboboxes de setores
        sourceDeptComboBox.removeAllItems();
        targetDeptComboBox.removeAllItems();
        
        for (Department dept : documentService.getAllDepartments()) {
            String item = dept.getCode() + " - " + dept.getName();
            sourceDeptComboBox.addItem(item);
            targetDeptComboBox.addItem(item);
        }
    }
    
    public static void main(String[] args) {
        // Usar o Event Dispatch Thread para criar a UI
        SwingUtilities.invokeLater(() -> {
            try {
                // Tentar usar o look and feel do sistema
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new DocumentManagementSystemUI();
        });
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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
    
    public Collection<Department> getAllDepartments() {
        return departments.values();
    }
    
    public void addDocument(Document document) {
        documents.put(document.getId(), document);
        System.out.println("Documento adicionado: " + document);
    }
    
    public Document getDocument(String id) {
        return documents.get(id);
    }
    
    public Collection<Document> getAllDocuments() {
        return documents.values();
    }
    
    public int getDocumentCountByDepartment(Department department) {
        return (int) documents.values().stream()
                .filter(doc -> doc.getCurrentDepartment().equals(department))
                .count();
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
    
    public String getDocumentHistory(String documentId) {
        Document document = documents.get(documentId);
        if (document == null) {
            return "Documento não encontrado: " + documentId;
        }
        
        StringBuilder history = new StringBuilder();
        history.append("Histórico de transferências para ").append(document.getTitle())
               .append(" (").append(document.getId()).append("):\n\n");
        
        for (TransferRecord record : document.getTransferHistory()) {
            history.append(record.toString()).append("\n");
        }
        
        return history.toString();
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
    
    public Map<String, ScheduledFuture<?>> getScheduledTasks() {
        return Collections.unmodifiableMap(scheduledTasks);
    }
}