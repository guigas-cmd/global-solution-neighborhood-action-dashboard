// Neighborhood Action Dashboard - Global Solution GS26_2
// Guilherme Dias - RM 576403 | Matheus Nunes - RM 574511
// CRUD simples para demonstrar tres regras de logica booleana.

import java.util.ArrayList;
import java.util.Scanner;

public class NeighborhoodActionDashboard {
    static Scanner teclado = new Scanner(System.in);
    static ArrayList<Ocorrencia> ocorrencias = new ArrayList<>();
    static int proximoId = 1;

    // Dados basicos de cada ocorrencia.
    static class Ocorrencia {
        int id;
        String tipo, descricao, localizacao = "", status = "Aberta";
        boolean urgente, coletivo, cadastro;
    }

    public static void main(String[] args) {
        int opcao;
        do {
            System.out.println("\n=== NEIGHBORHOOD ACTION DASHBOARD ===");
            System.out.println("1-Cadastrar  2-Consultar  3-Atualizar  4-Excluir");
            System.out.println("5-Indicadores  6-Testar regras  0-Sair");
            opcao = lerNumero("Opcao: ");
            switch (opcao) {
                case 1: cadastrar(); break;
                case 2: consultar(); break;
                case 3: atualizar(); break;
                case 4: excluir(); break;
                case 5: indicadores(); break;
                case 6: testarRegras(); break;
                case 0: System.out.println("Encerrado."); break;
                default: System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);
    }

    // CREATE: agora recebe a localizacao real, nao apenas s/n.
    static void cadastrar() {
        Ocorrencia o = new Ocorrencia();
        o.id = proximoId++;
        System.out.print("Tipo (iluminacao, limpeza etc.): ");
        o.tipo = teclado.nextLine();
        System.out.print("Descricao: ");
        o.descricao = teclado.nextLine();
        System.out.print("Localizacao (rua ou referencia; Enter se desconhecida): ");
        o.localizacao = teclado.nextLine().trim();
        o.urgente = lerSimNao("Urgente? (s/n): ");
        o.coletivo = lerSimNao("Afeta a coletividade? (s/n): ");
        o.cadastro = lerSimNao("Cadastro completo? (s/n): ");
        ocorrencias.add(o);
        System.out.println("Cadastrada com ID " + o.id + ".");
    }

    // READ: apresenta cada ocorrencia e as tres decisoes booleanas.
    static void consultar() {
        if (ocorrencias.isEmpty()) { System.out.println("Nenhuma ocorrencia."); return; }
        for (Ocorrencia o : ocorrencias) {
            boolean finalizada = o.status.equals("Concluida");
            boolean cancelada = o.status.equals("Cancelada");
            System.out.println("\nID " + o.id + " | " + o.tipo + " | " + o.status);
            System.out.println(o.descricao);
            System.out.println("Localizacao: " + (o.localizacao.isEmpty() ? "Nao informada" : o.localizacao));
            System.out.println("Prioridade: " + bit(prioridade(o.urgente, o.coletivo))
                + " | Pendencia: " + bit(pendencia(o.cadastro, !o.localizacao.isEmpty()))
                + " | Acompanhamento: " + bit(acompanhamento(finalizada, cancelada)));
        }
    }

    // UPDATE: permite corrigir status, cadastro ou localizacao.
    static void atualizar() {
        Ocorrencia o = buscar(lerNumero("ID: "));
        if (o == null) { System.out.println("ID nao encontrado."); return; }
        System.out.println("1-Status  2-Cadastro completo  3-Localizacao");
        int campo = lerNumero("O que atualizar? ");
        switch (campo) {
            case 1:
                System.out.println("1-Aberta  2-Em andamento  3-Concluida  4-Cancelada");
                int escolha = lerNumero("Novo status: ");
                String[] status = {"Aberta", "Em andamento", "Concluida", "Cancelada"};
                if (escolha < 1 || escolha > 4) { System.out.println("Status invalido."); return; }
                o.status = status[escolha - 1];
                break;
            case 2:
                o.cadastro = lerSimNao("Cadastro completo? (s/n): ");
                break;
            case 3:
                System.out.print("Nova localizacao (Enter para deixar vazia): ");
                o.localizacao = teclado.nextLine().trim();
                break;
            default:
                System.out.println("Opcao invalida.");
                return;
        }
        System.out.println("Ocorrencia atualizada.");
    }

    // DELETE: remove registros incorretos ou duplicados.
    static void excluir() {
        Ocorrencia o = buscar(lerNumero("ID: "));
        if (o == null) System.out.println("ID nao encontrado.");
        else { ocorrencias.remove(o); System.out.println("Registro excluido."); }
    }

    static Ocorrencia buscar(int id) {
        for (Ocorrencia o : ocorrencias) if (o.id == id) return o;
        return null;
    }

    // Regra 1 - AND: s = A * B (urgente E coletivo).
    static boolean prioridade(boolean A, boolean B) { return A && B; }

    // Regra 2 - NOT + OR: s = A' + B' (cadastro OU localizacao ausente).
    static boolean pendencia(boolean A, boolean B) { return !A || !B; }

    // Regra 3 - NOR: s = (A + B)' (nao concluida NEM cancelada).
    static boolean acompanhamento(boolean A, boolean B) { return !(A || B); }

    static void indicadores() {
        int p = 0, d = 0, a = 0;
        for (Ocorrencia o : ocorrencias) {
            if (prioridade(o.urgente, o.coletivo)) p++;
            if (pendencia(o.cadastro, !o.localizacao.isEmpty())) d++;
            if (acompanhamento(o.status.equals("Concluida"), o.status.equals("Cancelada"))) a++;
        }
        System.out.println("Total: " + ocorrencias.size() + " | Prioritarias: " + p
            + " | Pendencias: " + d + " | Acompanhamento: " + a);
    }

    // Compara todas as entradas possiveis com os resultados esperados.
    static void testarRegras() {
        int[][] tabela = {
            // A, B, esperado R1, esperado R2, esperado R3
            {0, 0, 0, 1, 1}, {0, 1, 0, 1, 0},
            {1, 0, 0, 1, 0}, {1, 1, 1, 0, 0}
        };
        int acertos = 0;
        System.out.println("A B | R1 R2 R3 | Testes");
        for (int[] linha : tabela) {
            boolean A = linha[0] == 1, B = linha[1] == 1;
            int[] saidas = {bit(prioridade(A, B)), bit(pendencia(A, B)),
                            bit(acompanhamento(A, B))};
            int corretos = 0;
            for (int i = 0; i < 3; i++) if (saidas[i] == linha[i + 2]) corretos++;
            acertos += corretos;
            System.out.println(linha[0] + " " + linha[1] + " |  " + saidas[0]
                + "  " + saidas[1] + "  " + saidas[2] + " | " + corretos + "/3 OK");
        }
        System.out.println("Total: " + acertos + "/12 testes corretos.");
    }

    static int bit(boolean valor) { return valor ? 1 : 0; }

    static boolean lerSimNao(String mensagem) {
        System.out.print(mensagem);
        String resposta = teclado.nextLine().trim();
        return resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("sim");
    }

    static int lerNumero(String mensagem) {
        System.out.print(mensagem);
        try { return Integer.parseInt(teclado.nextLine()); }
        catch (NumberFormatException e) { return -1; }
    }
}
