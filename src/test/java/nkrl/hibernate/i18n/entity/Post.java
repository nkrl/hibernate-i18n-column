package nkrl.hibernate.i18n.entity;

import jakarta.persistence.*;
import lombok.Data;
import nkrl.hibernate.i18n.Internationalized;

import java.util.Locale;

@Entity
@Table(name = "posts")
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @AttributeOverride(name = Internationalized.I18N_ATTRIBUTE_NAME, column = @Column(name = "titles"))
    private Internationalized<String> titles = new Internationalized<>();

    public void setTitle(Locale locale, String title) {
        titles.set(locale, title);
    }

    public String getTitle(Locale locale) {
        return titles.get(locale);
    }
}
