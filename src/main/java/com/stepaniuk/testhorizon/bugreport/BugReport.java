package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverity;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatus;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bug_reports")
public class BugReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bug_reports_id_gen")
    @SequenceGenerator(name = "bug_reports_id_gen", sequenceName = "bug_reports_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @ManyToOne
    @JoinColumn(name = "severity_id")
    private BugReportSeverity severity;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private BugReportStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        BugReport bugReport = (BugReport) o;
        return getId() != null && Objects.equals(getId(), bugReport.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "projectId = " + projectId + ", " +
                "title = " + title + ", " +
                "description = " + description + ", " +
                "environment = " + environment + ", " +
                "reporterId = " + reporterId + ", " +
                "severity = " + severity + ", " +
                "status = " + status + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}
