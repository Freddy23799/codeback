// file: codeqr/code/repository/SurveillantRepositoryCustom.java
package codeqr.code.repository;

import java.util.List;
import codeqr.code.dto.ResponsableListDTO;

public interface ResponsableRepositoryCustom {
    List<ResponsableListDTO> fetchResponsablesLight(Long cursorId, int limit, String q);
}
