package codeqr.code.dto;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VideLeCache {
@Autowired
  private EntityManager entityManager;

  public void clearfirst(){
    entityManager.clear();
  }

  public void clearsecond(){
    entityManager.getEntityManagerFactory().getCache().evictAll();
  }
  public void clearAll(){
    clearfirst();
    clearsecond();
  }
}
