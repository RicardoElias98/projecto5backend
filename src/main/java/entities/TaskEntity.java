package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name="Tasks")
@NamedQuery(name="Task.findTaskById", query="SELECT a FROM TaskEntity a WHERE a.id = :id")
@NamedQuery(name="Task.findTaskByUser", query="SELECT a FROM TaskEntity a WHERE a.user = :user")
@NamedQuery(name="Task.findTaskByCategory", query="SELECT a FROM TaskEntity a WHERE a.category = :category")
@NamedQuery(name="Task.findTaskByPriority", query="SELECT a FROM TaskEntity a WHERE a.priority = :priority")
@NamedQuery(name="Task.findBlockedTasks", query="SELECT a FROM TaskEntity a WHERE a.active = false")
@NamedQuery(name="Task.findUserById", query="SELECT a FROM TaskEntity a WHERE a.user = :user")
@NamedQuery(name="Task.findTaskByStatus", query="SELECT a FROM TaskEntity a WHERE a.status = :status")
@NamedQuery(name="Task.findTaskByUserAndCategory", query="SELECT a FROM TaskEntity a WHERE a.user = :user and a.category= : category")
@NamedQuery(name="Task.averageTasksPerUser", query="SELECT COUNT(t) / COUNT(DISTINCT t.user) FROM TaskEntity t")
@NamedQuery(name="Task.countTasksByStatus", query="SELECT COUNT(t) FROM TaskEntity t WHERE t.status = :status")
@NamedQuery(name="Task.countTasksByCategory", query="SELECT t.category, COUNT(t) FROM TaskEntity t GROUP BY t.category ORDER BY COUNT(t) DESC")
@NamedQuery(name="Task.countTasksByUser", query="SELECT t.user, COUNT(t) FROM TaskEntity t GROUP BY t.user")
@NamedQuery(name="Task.countTasksByUserAndStatus", query="SELECT t.user, t.status, COUNT(t) FROM TaskEntity t GROUP BY t.user, t.status")
@NamedQuery(name = "Task.countTasksByStatusAndActive", query = "SELECT COUNT(t) FROM TaskEntity t WHERE t.status = 30 AND t.active = true")
@NamedQuery(name = "Task.countTasksByRealFinalDate", query = "SELECT t.realFinalDate, COUNT(t) FROM TaskEntity t WHERE t.realFinalDate IS NOT NULL GROUP BY t.realFinalDate ORDER BY t.realFinalDate ASC"
)


public class TaskEntity implements Serializable {
    @Id
    @Column (name="id", nullable = false, unique = true, updatable = false)
    private String id;
    @Column (name="title", nullable = false, unique = true)
    private String title;
    @Column (name="description", nullable = true, unique = false, length = 65535, columnDefinition = "TEXT")
    private String description;
    @Column (name="priority", nullable = false, unique = false)
    private int priority;
    @Column (name="status", nullable = false, unique = false)
    private int status;
    @Column (name="startDate", nullable = false, unique = false)
    private LocalDate startDate;
    @Column (name="endDate", nullable = false, unique = false)
    private LocalDate endDate;
    @JoinColumn (name="user", nullable = false, unique = false)
    @ManyToOne
    private UserEntity user;
    @JoinColumn  (name="category", nullable = false, unique = false)
    @ManyToOne
    private CategoryEntity category;
    @Column (name="active", nullable = false, unique = false)
    private boolean active;

    @Column (name="realFinalDate", nullable= true, unique = false, updatable = true)
    private LocalDate realFinalDate;

    public LocalDate getRealFinalDate() {
        return realFinalDate;
    }

    public void setRealFinalDate(LocalDate realFinalDate) {
        this.realFinalDate = realFinalDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}



