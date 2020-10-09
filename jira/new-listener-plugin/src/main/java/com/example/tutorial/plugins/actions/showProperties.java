package com.example.tutorial.plugins.actions;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class showProperties extends JiraWebActionSupport {
    Logger LOG = LoggerFactory.getLogger(showProperties.class);
    private int   maxNumberOfUsers;
    private int minNumberOfUsers;
    private int deleteNumberOfUsers;
    private Properties properties;
    private static final long serialVersionUID = 1L;
    public showProperties(){
        try{
            properties = new Properties();
            InputStream fileStream = getClass().getClassLoader().getResourceAsStream("new-listener-plugin.properties");
            if(fileStream != null){
                properties.load(fileStream);
                maxNumberOfUsers = Integer.parseInt(properties.getProperty("maxNumberOfUsers"));
                minNumberOfUsers = Integer.parseInt(properties.getProperty("minNumberOfUsers"));
                deleteNumberOfUsers = Integer.parseInt(properties.getProperty("deleteNumberOfUsers"));
            }
            fileStream.close();
        }catch (Exception e){
            e.getMessage();
        }
    }
    @Override
    public String doDefault() throws Exception {
        // TODO Auto-generated method stub
        return super.doDefault();
    }
    @Override
    protected String doExecute() throws Exception {
        // TODO Auto-generated method stub
        return "input";
    }
    @Override
    protected void doValidation() {
        super.doValidation();
    }
    public int getMaxNumberOfUsers(){
        return this.maxNumberOfUsers;
    }
    public int getMinNumberOfUsers(){
        return this.minNumberOfUsers;
    }
    public int getDeleteNumberOfUsers(){
        return this.deleteNumberOfUsers;
    }
}
