package com.appsmith.server.repositories;

import com.appsmith.external.models.QActionConfiguration;
import com.appsmith.server.acl.AclPermission;
import com.appsmith.server.domains.NewAction;
import com.appsmith.server.domains.QNewAction;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class CustomNewActionRepositoryImpl extends BaseAppsmithRepositoryImpl<NewAction>
        implements CustomNewActionRepository {

    public CustomNewActionRepositoryImpl(ReactiveMongoOperations mongoOperations,
                                         MongoConverter mongoConverter) {
        super(mongoOperations, mongoConverter);
    }

    @Override
    public Mono<NewAction> findByUnpublishedNameAndPageId(String name, String pageId, AclPermission aclPermission) {
        Criteria nameCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.name)).is(name);
        Criteria pageCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.pageId)).is(pageId);

        return queryOne(List.of(nameCriteria, pageCriteria), aclPermission);
    }

    @Override
    public Flux<NewAction> findByPageId(String pageId, AclPermission aclPermission) {
        Criteria unpublishedPageCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.pageId)).is(pageId);
        Criteria publishedPageCriteria = where(fieldName(QNewAction.newAction.publishedAction.pageId)).is(pageId);

        Criteria pageCriteria = new Criteria().orOperator(unpublishedPageCriteria, publishedPageCriteria);

        return queryAll(List.of(pageCriteria), aclPermission);
    }

    @Override
    public Flux<NewAction> findByPageIdAndViewMode(String pageId, Boolean viewMode, AclPermission aclPermission) {
        Criteria pageCriteria;

        // Fetch published actions
        if (Boolean.TRUE.equals(viewMode)) {
            pageCriteria = where(fieldName(QNewAction.newAction.publishedAction.pageId)).is(pageId);
        }
        // Fetch unpublished actions
        else {
            pageCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.pageId)).is(pageId);
        }
        return queryAll(List.of(pageCriteria), aclPermission);
    }

    @Override
    public Flux<NewAction> findUnpublishedActionsByNameInAndPageIdAndActionConfiguration_HttpMethodAndUserSetOnLoad(
            Set<String> names,
            String pageId,
            String httpMethod,
            Boolean userSetOnLoad,
            AclPermission aclPermission) {
        Criteria namesCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.name)).in(names);
        Criteria pageCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.pageId)).is(pageId);
        Criteria userSetOnLoadCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.userSetOnLoad)).is(userSetOnLoad);
        String httpMethodQueryKey = fieldName(QNewAction.newAction.unpublishedAction.actionConfiguration)
                + "."
                + fieldName(QActionConfiguration.actionConfiguration.httpMethod);
        Criteria httpMethodCriteria = where(httpMethodQueryKey).is(httpMethod);
        List<Criteria> criterias = List.of(namesCriteria, pageCriteria, httpMethodCriteria, userSetOnLoadCriteria);

        return queryAll(criterias, aclPermission);
    }

    @Override
    public Flux<NewAction> findAllActionsByNameAndPageIdsAndViewMode(String name,
                                                                     List<String> pageIds,
                                                                     Boolean viewMode,
                                                                     AclPermission aclPermission,
                                                                     Sort sort) {
        /**
         * TODO : This function is called by get(params) to get all actions by params and hence
         * only covers criteria of few fields like page id, name, etc. Make this generic to cover
         * all possible fields
         */

        List<Criteria> criteriaList = new ArrayList<>();

        // Fetch published actions
        if (Boolean.TRUE.equals(viewMode)) {

            if (name != null) {
                Criteria nameCriteria = where(fieldName(QNewAction.newAction.publishedAction.name)).is(name);
                criteriaList.add(nameCriteria);
            }

            if (pageIds != null && !pageIds.isEmpty()) {
                Criteria pageCriteria = where(fieldName(QNewAction.newAction.publishedAction.pageId)).in(pageIds);
                criteriaList.add(pageCriteria);
            }
        }
        // Fetch unpublished actions
        else {

            if (name != null) {
                Criteria nameCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.name)).is(name);
                criteriaList.add(nameCriteria);
            }

            if (pageIds != null && !pageIds.isEmpty()) {
                Criteria pageCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.pageId)).in(pageIds);
                criteriaList.add(pageCriteria);
            }
        }

        return queryAll(criteriaList, aclPermission, sort);
    }

    @Override
    public Flux<NewAction> findUnpublishedActionsByNameInAndPageIdAndExecuteOnLoadTrue(Set<String> names,
                                                                                       String pageId,
                                                                                       AclPermission permission) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (names != null) {
            Criteria namesCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.name)).in(names);
            criteriaList.add(namesCriteria);
        }
        Criteria pageCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.pageId)).is(pageId);
        criteriaList.add(pageCriteria);

        Criteria executeOnLoadCriteria = where(fieldName(QNewAction.newAction.unpublishedAction.executeOnLoad)).is(Boolean.TRUE);
        criteriaList.add(executeOnLoadCriteria);

        return queryAll(criteriaList, permission);
    }

    @Override
    public Flux<NewAction> findByApplicationId(String applicationId, AclPermission aclPermission, Sort sort) {

        Criteria applicationCriteria = where(fieldName(QNewAction.newAction.applicationId)).is(applicationId);

        return queryAll(List.of(applicationCriteria), aclPermission, sort);
    }

    @Override
    public Flux<NewAction> findByApplicationIdAndNamesAndViewMode(String applicationId,
                                                                  Set<String> names,
                                                                  Boolean viewMode,
                                                                  AclPermission aclPermission) {

        Criteria applicationCriteria = where(fieldName(QNewAction.newAction.applicationId)).is(applicationId);

        Criteria nameCriterion;

        // Fetch published actions
        if (Boolean.TRUE.equals(viewMode)) {
            nameCriterion = where(fieldName(QNewAction.newAction.publishedAction.name)).in(names);
        }
        // Fetch unpublished actions
        else {
            nameCriterion = where(fieldName(QNewAction.newAction.unpublishedAction.name)).in(names);
        }

        return queryAll(List.of(applicationCriteria, nameCriterion), aclPermission);
    }
}