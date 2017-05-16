package net.slipp.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.slipp.CannotDeleteException;
import net.slipp.domain.Answer;
import net.slipp.domain.AnswerRepository;
import net.slipp.domain.DeleteHistory;
import net.slipp.domain.Question;
import net.slipp.domain.QuestionRepository;
import net.slipp.domain.User;

@Service("qnaService")
public class QnaService {
    private static final Logger log = LoggerFactory.getLogger(QnaService.class);

    @Resource(name = "questionRepository")
    private QuestionRepository questionRepository;

    @Resource(name = "answerRepository")
    private AnswerRepository answerRepository;

    @Resource(name = "deleteHistoryService")
    private DeleteHistoryService deleteHistoryService;

    public Question create(User loginUser, Question question) {
        question.writeBy(loginUser);
        log.debug("question : {}", question);
        return questionRepository.save(question);
    }

    public Question findById(long id) {
        return questionRepository.findOne(id);
    }

    public Question update(User loginUser, long id, Question updatedQuestion) {
        Question question = questionRepository.findOne(id);
        question.update(loginUser, updatedQuestion);
        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(User loginUser, long questionId) throws CannotDeleteException {
        Question question = questionRepository.findOne(questionId);
        if (question == null) {
            throw new EmptyResultDataAccessException("존재하지 않는 질문입니다.", 1);
        }

        List<DeleteHistory> histories = question.delete(loginUser);
        deleteHistoryService.saveAll(histories);
    }

    public Iterable<Question> findAll() {
        return questionRepository.findByDeleted(false);
    }

    public List<Question> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable).getContent();
    }

    public Answer addAnswer(User loginUser, long questionId, String contents) {
        Question question = questionRepository.findOne(questionId);
        Answer answer = new Answer(loginUser, contents);
        question.addAnswer(answer);
        questionRepository.save(question);
        return answer;
    }

    public Answer deleteAnswer(User loginUser, long id) {
        Answer answer = answerRepository.findOne(id);
        answer.deletedBy(loginUser);
        answerRepository.delete(answer);
        return answer;
    }
}
