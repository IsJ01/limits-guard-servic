package isj01.limits.guard.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "merchant_id")
    private String merchantId;

}
