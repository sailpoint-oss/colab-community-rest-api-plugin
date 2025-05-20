package sailpoint.community.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;
import sailpoint.object.Rule;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Object resource.
 */
@RequiredRight(value = "communityRestObjectResource")
@Path("/Object")
public class ObjectResource extends BasePluginResource {
    /**
     * The constant log.
     */
    public static final Log log = LogFactory.getLog(ObjectResource.class);
    private static SailPointContext context;


    private void initConfig() throws GeneralException {
        context = SailPointFactory.getCurrentContext();
    }


    /**
     * Gets rule.
     *
     * @param name the name
     * @return the rule
     * @throws GeneralException  the general exception
     * @throws DocumentException the document exception
     * @throws IOException       the io exception
     */
    @GET
    @Path("/getRule/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRule(@PathParam("name") String name) throws GeneralException, DocumentException, IOException {
        initConfig();
        Rule rule;
        String ruleXml;
        try {
            rule = context.getObject(Rule.class, name);
            if (rule == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Rule not found").build();
            }
            ruleXml = rule.toXml(true);

        } catch (GeneralException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        // Pretty print the XML
        Document doc = DocumentHelper.parseText(ruleXml);
        OutputFormat format = OutputFormat.createPrettyPrint();
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(doc);
        ruleXml = writer.toString();
        return Response.ok(ruleXml).build();
    }

    /**
     * Gets workgroups.
     *
     * @param filter the filter
     * @return the workgroups
     * @throws GeneralException the general exception
     */
    @RequiredRight(value = "communityRestObjectResourceGetWorkgroups")
    @GET
    @Path("/Workgroup/{filter}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkgroups(@PathParam("filter") String filter) throws GeneralException {
        initConfig();
        QueryOptions qo = new QueryOptions();
        List<HashMap<String, Object>> returnList = new ArrayList<>();
        qo.add(Filter.eq("workgroup", true));
        if(filter != null && !filter.isEmpty()) {
            qo.add(Filter.eq("name", filter));
        }
        List<Identity> workgroups = context.getObjects(Identity.class, qo);
        for (Identity workgroup : workgroups) {
            HashMap<String, Object> workgroupsResult = new HashMap<>();


            workgroupsResult.put("id", workgroup.getId());
            workgroupsResult.put("name", workgroup.getName());
            workgroupsResult.put("description", workgroup.getDescription());
            workgroupsResult.put("displayName", workgroup.getDisplayName());
            Identity owner = workgroup.getOwner();
            if (owner != null) {
                workgroupsResult.put("ownerId", owner.getName());
            }
            workgroupsResult.put("preferences", workgroup.getPreferences());
            workgroupsResult.put("created", workgroup.getCreated());
            workgroupsResult.put("modified", workgroup.getModified());
            workgroupsResult.put("lastModified", workgroup.getModified());
            workgroupsResult.put("significantModified", workgroup.getSignificantModified());
            if (workgroup.getAssignedScope() != null) {
                workgroupsResult.put("assignedScope", workgroup.getAssignedScope().getName());
            }
            List controlledScopes = workgroup.getControlledScopes();
            if (controlledScopes != null && !controlledScopes.isEmpty()) {
                List<String> controlledScopesNames = new ArrayList<>();
                for (Object controlledScope : controlledScopes) {
                    controlledScopesNames.add(((sailpoint.object.Scope) controlledScope).getName());
                }
                workgroupsResult.put("controlledScopes", controlledScopesNames);
            }
            List capabilities = workgroup.getCapabilities();
            if (capabilities != null && !capabilities.isEmpty()) {
                List<String> capabilitiesNames = new ArrayList<>();
                for (Object capability : capabilities) {
                    capabilitiesNames.add(((sailpoint.object.Capability) capability).getName());
                }
                workgroupsResult.put("capabilities", capabilitiesNames);
            }
            workgroupsResult.put("attributes", workgroup.getAttributes());
            workgroupsResult.put("workgroup", true);
            returnList.add(workgroupsResult);


        }
        log.error(returnList.toString());
        return Response.status(200).entity(returnList).build();
    }
    @RequiredRight(value = "communityRestObjectResourceGetWorkgroups")
    @GET
    @Path("/Workgroup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkgroupsWithNoFilter() throws GeneralException {
        return Response.status(200).entity(this.getWorkgroups(null)).build();
    }
    @Override
    public String getPluginName() {
        return "communityrestapi";
    }
}
