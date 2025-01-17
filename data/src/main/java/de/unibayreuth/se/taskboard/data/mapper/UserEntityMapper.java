package de.unibayreuth.se.taskboard.data.mapper;

import de.unibayreuth.se.taskboard.business.domain.User;
import de.unibayreuth.se.taskboard.data.persistence.UserEntity;
import org.mapstruct.Mapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@Mapper(componentModel = "spring")
@ConditionalOnMissingBean // prevent IntelliJ warning about duplicate beans
public interface UserEntityMapper {
    UserEntity toEntity(User source);
    User fromEntity(UserEntity source);
}
