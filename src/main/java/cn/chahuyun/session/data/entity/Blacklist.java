package cn.chahuyun.session.data.entity;

import cn.chahuyun.session.data.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 黑名单
 *
 * @author Moyuyanli
 * @date 2024/1/15 10:45
 */
@Entity
@Getter
@Setter
@Table(name = "black_list")
public class Blacklist extends BaseEntity {


}
