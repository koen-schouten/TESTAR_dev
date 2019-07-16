package nl.ou.testar.temporal.structure;

import nl.ou.testar.StateModel.Persistence.OrientDB.Util.Validation;
import nl.ou.testar.temporal.util.InferrableExpression;
import nl.ou.testar.temporal.util.PairBean;
import nl.ou.testar.temporal.util.TagBean;
import org.fruit.alayer.Role;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;
import org.fruit.alayer.windows.UIARoles;
import org.fruit.alayer.windows.UIATags;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class WidgetFilter {
    private Set<String> widgetRolesMatches;
    private Set<String> widgetTitleMatches;
    private Set<String> widgetPathMatches;
    private Set<String> widgetParentTitleMatches;
    private Set<TagBean<?>> selectedAttributes;
    //private static Set<PairBean<InferrableExpression,String>> defaultValuedExpressions= new LinkedHashSet<>();
    private  Set<PairBean<InferrableExpression,String>> valuedExpressions = new LinkedHashSet<>();


    public WidgetFilter() {
        widgetRolesMatches = new LinkedHashSet<>();
        widgetTitleMatches= new LinkedHashSet<>();
        widgetPathMatches= new LinkedHashSet<>();
        widgetParentTitleMatches= new LinkedHashSet<>();
        selectedAttributes=new LinkedHashSet<>();
        valuedExpressions =new LinkedHashSet<>();
    }
   public WidgetFilter(Set<PairBean<InferrableExpression,String>> valuedExpressions) {  //candidate fo refactoring: valueexpression
       this();

            this.setDefaultRoleMatches(valuedExpressions);
            this.setDefaultTitleMatches(valuedExpressions);
            this.setDefaultPathMatches(valuedExpressions);
            this.setDefaultParentTitleMatches(valuedExpressions);
            selectedAttributes=getEntireTagSet();
            this.valuedExpressions = useSimpleValuedExpressions();

    }
    public Set<TagBean<?>> getSelectedAttributes() {
        return selectedAttributes;
    }

    public void setSelectedAttributes(Set<TagBean<?>> selectedAttributes) {
        this.selectedAttributes = selectedAttributes;
    }

    private Set<TagBean<?>> getEntireTagSet(){


        // WORKAROUND CSS 20190629
        // the 2 dummy reads are required to ensure properly initialization of the classes: static method/property is used!
        // both classes Tags and UIATags inherit from abstract class TagBase
        //without this initialization, the call to .tagset() from either class collides into the same tagset content.
        // symptom: UIATags appears to have the same tags as Tags and we're missing out on the real UIATags.

        Tag<?> dummy = UIATags.UIAItemType;
        dummy= Tags.Enabled;

        Set<Tag<?>> tags = new HashSet<Tag<?>>();
        tags.addAll(Tags.tagSet());
        tags.addAll(UIATags.tagSet());//alternative for platform independent is : getNativetags ??
        Set<TagBean<?>> tmptagset=new LinkedHashSet<>();
        Iterator<Tag<?>> iterator;
        for (iterator = tags.iterator(); iterator.hasNext(); ) {
            Tag<?> t = iterator.next();
            TagBean<?> t1 = TagBean.from(Validation.sanitizeAttributeName(t.name()), t.type()); //orientdb style tags
            tmptagset.add(t1);
        }
        return  tmptagset;
    };
    public Set<PairBean<InferrableExpression, String>> getValuedExpressions() {
        return valuedExpressions;
    }


    public void setValuedExpressions(Set<PairBean<InferrableExpression, String>> valuedExpressions) {
        this.valuedExpressions = valuedExpressions;
    }

    public static Set<PairBean<InferrableExpression,String>> useSimpleValuedExpressions() {
        Set<PairBean<InferrableExpression,String>> simve = new LinkedHashSet<>();
        simve.add(new PairBean<>(InferrableExpression.is_blank_, ""));  // use always
        simve.add(new PairBean<>(InferrableExpression.exists_, ""));// use always
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:OK)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:CANCEL)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:YES)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:NO)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:GO)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:RUN)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:SAVE)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:EXIT)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:CLOSE)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:REMOVE)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:ERROR)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:SUBMIT)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:OPEN)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:IGNORE)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:PROCEED)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:PRINT)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:VIEW)"));
        simve.add(new PairBean<>(InferrableExpression.textmatch_, "")); //no title
        simve.add(new PairBean<>(InferrableExpression.width_lt_, "100"));
        simve.add(new PairBean<>(InferrableExpression.textlength_lt_, "20"));
        simve.add(new PairBean<>(InferrableExpression.rolematch_, UIARoles.UIAButton.toString()));
        simve.add(new PairBean<>(InferrableExpression.rolematch_, UIARoles.UIAWindow.toString()));
return simve;

    }

    public static Set<PairBean<InferrableExpression,String>>  useDefaultValuedExpressions() {
        Set<PairBean<InferrableExpression,String>> defve = new LinkedHashSet<>();
        defve.add(new PairBean<>(InferrableExpression.is_blank_, ""));  // use always
        defve.add(new PairBean<>(InferrableExpression.exists_, ""));// use always
        defve.add(new PairBean<>(InferrableExpression.value_eq_, "0"));
        defve.add(new PairBean<>(InferrableExpression.value_eq_, "1"));
        defve.add(new PairBean<>(InferrableExpression.value_eq_, "2"));
        defve.add(new PairBean<>(InferrableExpression.value_lt_, "10"));
        defve.add(new PairBean<>(InferrableExpression.value_lt_, "100"));
        defve.add(new PairBean<>(InferrableExpression.value_lt_, "1000"));
        defve.add(new PairBean<>(InferrableExpression.value_lt_, "10000"));
        defve.add(new PairBean<>(InferrableExpression.value_lt_, "100000"));
        defve.add(new PairBean<>(InferrableExpression.value_lt_, "1000000"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:OK)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:CANCEL)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:YES)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:NO)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:GO)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:RUN)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:SAVE)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:EXIT)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:CLOSE)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:REMOVE)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:ERROR)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:SUBMIT)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:OPEN)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:IGNORE)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:PROCEED)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:PRINT)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:VIEW)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:UP)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:DOWN)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:LEFT)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "(?i:RIGHT)"));
        defve.add(new PairBean<>(InferrableExpression.textmatch_, "")); //no title
        defve.add(new PairBean<>(InferrableExpression.pathmatch_, ".*\\[(\\d+, )*\\d+\\]"));
        defve.add(new PairBean<>(InferrableExpression.heigth_lt_, "50"));
        defve.add(new PairBean<>(InferrableExpression.heigth_lt_, "250"));
        defve.add(new PairBean<>(InferrableExpression.heigth_lt_, "500"));
        defve.add(new PairBean<>(InferrableExpression.heigth_lt_, "1000"));
        defve.add(new PairBean<>(InferrableExpression.width_lt_, "50"));
        defve.add(new PairBean<>(InferrableExpression.width_lt_, "250"));
        defve.add(new PairBean<>(InferrableExpression.width_lt_, "500"));
        defve.add(new PairBean<>(InferrableExpression.width_lt_, "1000"));
        defve.add(new PairBean<>(InferrableExpression.textlength_eq_, "1"));
        defve.add(new PairBean<>(InferrableExpression.textlength_eq_, "2"));
        defve.add(new PairBean<>(InferrableExpression.textlength_eq_, "3"));
        defve.add(new PairBean<>(InferrableExpression.textlength_lt_, "10"));
        defve.add(new PairBean<>(InferrableExpression.textlength_lt_, "20"));
        defve.add(new PairBean<>(InferrableExpression.textlength_lt_, "50"));
        defve.add(new PairBean<>(InferrableExpression.textlength_lt_, "100"));
        defve.add(new PairBean<>(InferrableExpression.textlength_lt_, "200"));
        defve.add(new PairBean<>(InferrableExpression.relpos_upleft_, ""));
        defve.add(new PairBean<>(InferrableExpression.relpos_upright_, ""));
        defve.add(new PairBean<>(InferrableExpression.relpos_downleft_, ""));
        defve.add(new PairBean<>(InferrableExpression.relpos_downright_, ""));
        defve.add(new PairBean<>(InferrableExpression.rolematch_, UIARoles.UIAButton.toString()));
        defve.add(new PairBean<>(InferrableExpression.rolematch_, UIARoles.UIAWindow.toString()));
        defve.add(new PairBean<>(InferrableExpression.rolematch_, UIARoles.UIACheckBox.toString()));
        defve.add(new PairBean<>(InferrableExpression.rolematch_, UIARoles.UIARadioButton.toString()));
        defve.add(new PairBean<>(InferrableExpression.rolematch_, UIARoles.UIAEdit.toString()));
return defve;
    }


    public Set<String> getWidgetRolesMatches() {
        return widgetRolesMatches;
    }



    public void setWidgetRolesMatches(Set<String> widgetRolesMatches) {
        this.widgetRolesMatches = widgetRolesMatches;
    }

    public Set<String> getWidgetTitleMatches() {
        return widgetTitleMatches;
    }

    public void setWidgetTitleMatches(Set<String> widgetTitleMatches) {
        this.widgetTitleMatches = widgetTitleMatches;
    }

    public Set<String> getWidgetPathMatches() {
        return widgetPathMatches;
    }

    public void setWidgetPathMatches(Set<String> widgetPathMatches) {
        this.widgetPathMatches = widgetPathMatches;
    }

    public Set<String> getWidgetParentTitleMatches() {
        return widgetParentTitleMatches;
    }

    public void setWidgetParentTitleMatches(Set<String> widgetParentTitleMatches) {
        this.widgetParentTitleMatches = widgetParentTitleMatches;
    }

    public void addWidgetRoleMatch(String expr){
        this.widgetRolesMatches.add(expr);
    }
    public void addWidgetTitleMatch(String expr){
        this.widgetTitleMatches.add(expr);
    }
    public void addWidgetPathMatch(String expr){
        this.widgetPathMatches.add(expr);
    }
    public void addWidgetparentTitleMatch(String expr){
        this.widgetParentTitleMatches.add(expr);
    }


    //custom
    public void setAllAvailableWidgetRoles(){
        for (Role r:UIARoles.rolesSet()
             ) {
            this.widgetRolesMatches.add(r.toString());
        }
    }

    public void setDefaultRoleMatches(Set<PairBean<InferrableExpression,String>> valuedExpressions){
        widgetRolesMatches.clear();
        for (PairBean<InferrableExpression,String> iap: valuedExpressions
        )        {
            if (iap.left()==InferrableExpression.rolematch_){
                widgetRolesMatches.add(iap.right());
            }
        }
    }

    public void setDefaultTitleMatches(Set<PairBean<InferrableExpression,String>> valuedExpressions){
        widgetTitleMatches.clear();
        for (PairBean<InferrableExpression,String> iap: valuedExpressions
             )        {
           if (iap.left()==InferrableExpression.textmatch_){
               widgetTitleMatches.add(iap.right());
           }
        }
    }
    public void setDefaultParentTitleMatches(Set<PairBean<InferrableExpression,String>> valuedExpressions){
        widgetParentTitleMatches.clear();
        for (PairBean<InferrableExpression,String> iap: valuedExpressions
        )        {
            if (iap.left()==InferrableExpression.textmatch_){
                widgetParentTitleMatches.add(iap.right());
            }
        }
    }
    public void setDefaultPathMatches(Set<PairBean<InferrableExpression,String>> valuedExpressions){
        widgetPathMatches.clear();
        for (PairBean<InferrableExpression,String> iap: valuedExpressions
        )        {
            if (iap.left()==InferrableExpression.pathmatch_){
                widgetPathMatches.add(iap.right());
            }
        }
    }

}

