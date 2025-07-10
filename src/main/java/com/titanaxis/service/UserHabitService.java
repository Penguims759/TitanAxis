package com.titanaxis.service;

import com.google.inject.Inject;
import com.titanaxis.exception.PersistenciaException;
import com.titanaxis.model.Usuario;
import com.titanaxis.model.auditoria.Habito;
import com.titanaxis.repository.AuditoriaRepository;
import com.titanaxis.repository.HabitoRepository;
import com.titanaxis.repository.UsuarioRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserHabitService {

    private final AuditoriaRepository auditoriaRepository;
    private final HabitoRepository habitoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransactionService transactionService;

    @Inject
    public UserHabitService(AuditoriaRepository auditoriaRepository, HabitoRepository habitoRepository, UsuarioRepository usuarioRepository, TransactionService transactionService) {
        this.auditoriaRepository = auditoriaRepository;
        this.habitoRepository = habitoRepository;
        this.usuarioRepository = usuarioRepository;
        this.transactionService = transactionService;
    }

    public List<Habito> findHabitsForUser(int usuarioId) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> habitoRepository.findByUsuario(usuarioId, em));
    }

    public List<Habito> findHabitsForToday(int usuarioId) throws PersistenciaException {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<Habito> allHabits = findHabitsForUser(usuarioId);
        return allHabits.stream()
                .filter(h -> h.getDiaDaSemana() == today)
                .collect(Collectors.toList());
    }

    public void learnNewHabits(int usuarioId) throws PersistenciaException {
        List<Object[]> historico = transactionService.executeInTransactionWithResult(em ->
                auditoriaRepository.findUserActionsForHabitAnalysis(usuarioId, 90, em)
        );

        Map<String, Map<DayOfWeek, Long>> habitosContados = historico.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0],
                        Collectors.groupingBy(
                                row -> ((java.sql.Timestamp) row[1]).toLocalDateTime().getDayOfWeek(),
                                Collectors.counting()
                        )
                ));

        transactionService.executeInTransaction(em -> {
            Optional<Usuario> userOpt = usuarioRepository.findById(usuarioId, em);
            if(userOpt.isEmpty()) return;
            Usuario user = userOpt.get();

            List<Habito> habitosExistentes = habitoRepository.findByUsuario(usuarioId, em);

            habitosContados.forEach((acao, mapaDias) -> {
                mapaDias.forEach((dia, contagem) -> {
                    if (contagem > 2) { // Considera um hábito se ocorreu mais de 2 vezes
                        boolean jaExiste = habitosExistentes.stream()
                                .anyMatch(h -> h.getAcao().equals(acao) && h.getDiaDaSemana() == dia);
                        if (!jaExiste) {
                            Habito novoHabito = new Habito();
                            novoHabito.setUsuario(user);
                            novoHabito.setAcao(acao);
                            novoHabito.setDiaDaSemana(dia);
                            novoHabito.setTipo(Habito.TipoHabito.AUTOMATICO);
                            habitoRepository.save(novoHabito, em);
                        }
                    }
                });
            });
        });
    }

    public Habito createManualHabit(int usuarioId, String acao, DayOfWeek diaDaSemana) throws PersistenciaException {
        return transactionService.executeInTransactionWithResult(em -> {
            Optional<Usuario> userOpt = usuarioRepository.findById(usuarioId, em);
            if(userOpt.isEmpty()) {
                throw new IllegalArgumentException("Utilizador não encontrado.");
            }

            Habito novoHabito = new Habito();
            novoHabito.setUsuario(userOpt.get());
            novoHabito.setAcao(acao);
            novoHabito.setDiaDaSemana(diaDaSemana);
            novoHabito.setTipo(Habito.TipoHabito.MANUAL);

            return habitoRepository.save(novoHabito, em);
        });
    }
}