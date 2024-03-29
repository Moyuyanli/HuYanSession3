package cn.chahuyun.session.data.entity;

import cn.chahuyun.session.data.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 多词条消息
 *
 * @author Moyuyanli
 * @date 2024/1/8 16:54
 */
@Entity
@Getter
@Setter
@Table(name = "session_many")
public class ManySession extends BaseEntity {


}
