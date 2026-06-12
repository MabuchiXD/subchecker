package org.example.subchecker.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.subchecker.core.entity.BotSession;
import org.example.subchecker.core.model.Currency;
import org.example.subchecker.core.repository.BotSessionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotSessionService {
    private final BotSessionRepository botSessionRepository;

    public BotSession getSession(Long userId){
        return botSessionRepository.findById(userId).orElseGet(()->botSessionRepository.save(BotSession.builder().userId(userId).state("IDLE").build()));
    }

    @Transactional
    public void updateState(Long userId, String state){
        BotSession session = getSession(userId);
        session.setState(state);
        botSessionRepository.save(session);
    }

    @Transactional
    public void saveTempName(Long userId, String name){
        BotSession session = getSession(userId);
        session.setTempName(name);
        botSessionRepository.save(session);
    }

    @Transactional
    public void saveTempPrice(Long userId, Double price){
        BotSession session = getSession(userId);
        session.setTempPrice(price);
        botSessionRepository.save(session);
    }
    @Transactional
    public void saveTempUrl(Long userId, String url){
        BotSession session = getSession(userId);
        session.setTempUrl(url);
        botSessionRepository.save(session);
    }
    @Transactional
    public void saveTempCategory(Long userId, String category){
        BotSession session = getSession(userId);
        session.setTempCategory(category);
        botSessionRepository.save(session);
    }
    @Transactional
    public void saveTempPeriod(Long userId, Integer days){
        BotSession session = getSession(userId);
        session.setTempPeriodDays(days);
        botSessionRepository.save(session);
    }
    @Transactional
    public void saveTempSubId(Long userId, Long subId){
        BotSession session = getSession(userId);
        session.setTempSubId(subId);
        botSessionRepository.save(session);
    }
    @Transactional
    public void saveTempNotes(Long userId, String notes){
        BotSession session = getSession(userId);
        session.setTempNotes(notes);
        botSessionRepository.save(session);
    }

    @Transactional
    public void saveTempDefaultCurrency(Long userId, Currency currency) {
        BotSession session = getSession(userId);
        session.setTempDefaultCurrency(currency);
        botSessionRepository.save(session);
    }

    @Transactional
    public void saveTempPrefTime(Long userId, java.time.LocalTime time) {
        BotSession session = getSession(userId);
        session.setTempPrefTime(time);
        botSessionRepository.save(session);
    }
    @Transactional
    public void saveTempTimezone(Long userId, Integer offset) {
        BotSession session = getSession(userId);
        session.setTempTimezoneOffset(offset);
        botSessionRepository.save(session);
    }
    @Transactional
    public void saveTempCurrency(Long userId, Currency currency) {
        BotSession session = getSession(userId);
        session.setTempCurrency(currency);
        botSessionRepository.save(session);
    }

}
