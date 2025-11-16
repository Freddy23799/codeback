// file: codeqr/code/repository/SurveillantRepositoryCustom.java
package codeqr.code.repository;

import java.util.List;
import codeqr.code.dto.SurveillantListDTO;

public interface SurveillantRepositoryCustom {
    List<SurveillantListDTO> fetchSurveillantsLight(Long cursorId, int limit, String q);
}
