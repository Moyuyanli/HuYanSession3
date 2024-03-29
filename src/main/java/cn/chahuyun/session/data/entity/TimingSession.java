package cn.chahuyun.session.data.entity;

import cn.chahuyun.session.data.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 定时消息
 *
 * @author Moyuyanli
 * @date 2024/1/8 16:58
 */
@Entity
@Getter
@Setter
@Table(name = "session_timing")
public class TimingSession extends BaseEntity {

}
