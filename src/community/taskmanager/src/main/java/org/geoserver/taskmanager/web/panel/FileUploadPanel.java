/* (c) 2018 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.taskmanager.web.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.taskmanager.external.FileService;
import org.geoserver.taskmanager.util.TaskManagerBeans;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.ParamResourceModel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Upload a File with the @{FileService} .
 *
 * @author Timothy De Bock
 */
public class FileUploadPanel extends Panel {

    private FeedbackPanel feedbackPanel;

    private static final long serialVersionUID = -1821529746678003578L;


    private IModel<String> fileNameModel;

    private final GeoServerDialog dialog;

    /**
     * Form for uploads.
     */
    private FileUploadField fileUploadField;

    private DropDownChoice<String> fileServiceChoice;

    private DropDownChoice<String> folderChoice;

    private AjaxSubmitLink addFolderButton;

    /**
     * Construct.
     *
     * @param id          Component name
     * @param fileNameModel the model that will contain the name of the file.
     */

    public FileUploadPanel(String id, IModel<String> fileNameModel) {

        super(id);
        this.fileNameModel = fileNameModel;

        add(dialog = new GeoServerDialog("dialog"));
        add(feedbackPanel = new FeedbackPanel("feedback"));
        feedbackPanel.setOutputMarkupId(true);


        fileServiceChoice =
                new DropDownChoice<String>("fileServiceSelection", new Model<String>(),
                        new ArrayList<>(TaskManagerBeans.get().getFileServices().names()),
                        new IChoiceRenderer<String>() {
                            private static final long serialVersionUID = -1102965730550597918L;

                            @Override
                            public Object getDisplayValue(String object) {
                                return TaskManagerBeans.get().getFileServices().get(object).getDescription();
                            }

                            @Override
                            public String getIdValue(String object, int index) {
                                return object;
                            }

                            @Override
                            public String getObject(String id, IModel<? extends List<? extends String>> choices) {
                                return id;
                            }                   
                    
                }) {
                    private static final long serialVersionUID = 2231004332244002574L;

                    @Override
                    public boolean isRequired() {
                        return hasBeenSubmitted();
                    }
                };
        add(fileServiceChoice.setNullValid(false));

        folderChoice = new DropDownChoice<String>("folderSelection", new Model<String>(), new ArrayList<>()) {
            private static final long serialVersionUID = 3543687800810146647L;

            @Override
            public boolean isRequired() {
                return hasBeenSubmitted();
            }
        };
        folderChoice.setOutputMarkupId(true);
        add(folderChoice);

        fileServiceChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String serviceName = fileServiceChoice.getModel().getObject();
                List<String> availableFolders = new ArrayList<String>();
                if (serviceName != null) {
                    try {
                        List<String> paths =
                                TaskManagerBeans.get().getFileServices().get(serviceName).listSubfolders();
                        for (String path : paths) {
                            availableFolders.add(path);
                        }
                    } catch (IOException e) {
                        FileUploadPanel.this.error("Could not get folders for service:" + e.getMessage());
                    }
                }
                folderChoice.setChoices(availableFolders);
                target.add(folderChoice);
                addFolderButton.setVisible(serviceName != null);
                target.add(addFolderButton);
            }
        });

        addFolderButton = createAddFolderButton(folderChoice);
        addFolderButton.setVisible(false);
        addFolderButton.setOutputMarkupPlaceholderTag(true);

        add(addFolderButton);
        add(fileUploadField = new FileUploadField("fileInput") {
            private static final long serialVersionUID = 4614183848423156996L;

            @Override
            public boolean isRequired() {
                return hasBeenSubmitted();
            }
        });
    }

    protected boolean hasBeenSubmitted() {
        Form<?> dialogForm = (Form<?>) getParent();
        return dialogForm.findSubmittingButton() == dialogForm.get("submit");
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        ((Form<?>) getParent()).setMultiPart(true);
    }

    public void onSubmit() {
        final List<FileUpload> uploads = fileUploadField.getFileUploads();
        if (uploads != null) {
            for (FileUpload upload : uploads) {
                FileService fileService =
                        TaskManagerBeans.get().getFileServices().get(fileServiceChoice.getModelObject());
                try {
                    String filePath = folderChoice.getModelObject() + "/" + upload.getClientFileName();
                    if (fileService.checkFileExists(filePath)) {
                        fileService.delete(filePath);
                    }
                    String path = fileService.create(filePath, upload.getInputStream());

                    fileNameModel.setObject(path);
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to write file", e);
                }
            }
        }
    }

    /**
     * .
     * Create the 'add new folder' button.
     *
     * @param folderChoice
     * @return
     */
    protected AjaxSubmitLink createAddFolderButton(DropDownChoice<String> folderChoice) {
        return new AjaxSubmitLink("addNew") {

            private static final long serialVersionUID = 7320342263365531859L;

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                dialog.setTitle(new ParamResourceModel("createFolder", FileUploadPanel.this));
                dialog.setInitialHeight(100);
                dialog.setInitialWidth(630);
                dialog.showOkCancel(target, new GeoServerDialog.DialogDelegate() {

                    private static final long serialVersionUID = 7410393012930249966L;

                    private TextFieldPanel panel;

                    @Override
                    protected Component getContents(String id) {
                        panel = new TextFieldPanel(id, new Model<>());
                        panel.add(new PreventSubmitOnEnterBehavior());
                        panel.getTextField().setRequired(true);
                        panel.setOutputMarkupId(true);
                        return panel;
                    }

                    @Override
                    protected boolean onSubmit(AjaxRequestTarget target, Component contents) {
                        target.add(panel);

                        List<String> availableFolders = new ArrayList<String>();
                        availableFolders.addAll(folderChoice.getChoicesModel().getObject());
                        String folderName = panel.getTextField().getModel().getObject();
                        availableFolders.add(folderName);
                        folderChoice.setChoices(availableFolders);
                        folderChoice.setModelObject(folderName);
                        target.add(folderChoice);
                        return true;
                    }

                });
            }
        };
    }


    public FeedbackPanel getFeedbackPanel() {
        return feedbackPanel;
    }

    public class PreventSubmitOnEnterBehavior extends Behavior {
        private static final long serialVersionUID = 1496517082650792177L;

        public PreventSubmitOnEnterBehavior() {
        }

        @Override
        public void bind(Component component) {
            super.bind(component);

            component.add(AttributeModifier.replace("onkeydown", Model.of("if(event.keyCode == 13) {event.preventDefault();}")));
        }
    }

}