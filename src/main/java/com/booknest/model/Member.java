package com.booknest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Tên thành viên không được để trống")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(name = "phone", nullable = false)
    private String phone;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MemberProfile profile;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans = new ArrayList<>();

    public void setProfile(MemberProfile profile) {
        this.profile = profile;
        if (profile != null) {
            profile.setMember(this);
        }
    }
    public void addLoan(Loan loan) {
        this.loans.add(loan);
        loan.setMember(this);
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public MemberProfile getProfile() { return profile; }
    public List<Loan> getLoans() { return loans; }
    public void setLoans(List<Loan> loans) { this.loans = loans; }
}