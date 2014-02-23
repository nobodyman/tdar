-- jdevos 8/20/2013
-- upgrade script for knap
-- add sensory data records to the dataset join-table
insert into dataset(id) select id from sensory_data sd where not exists (select * from dataset ds where ds.id = sd.id);

-- abrin 8/22/2013
alter table geospatial drop column projection;
alter table geospatial add column map_source varchar(500);

-- mpaulo 09/02/2013 TDAR-3006
alter table archive add column doimportcontent boolean default false;
alter table archive add column importdone boolean default false;

-- mpaulo 09/11/2013 TDAR-3018
CREATE TABLE audio (
    id bigint NOT NULL,
    audio_codec varchar(255),
    software varchar(255),
    bit_depth int4,
    bit_rate int4,
    sample_rate int4,
    CONSTRAINT audio_pkey PRIMARY KEY (id ),
    CONSTRAINT audio_fkey FOREIGN KEY (id)
    REFERENCES information_resource (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
) WITH (
    OIDS=FALSE
);
ALTER TABLE audio OWNER TO tdar;

-- jdevos 9/5/2013
alter table person add column tos_level integer not null default 0;
alter table person add column creator_agreement_level integer not null default 0;

alter table person rename column tos_level  to tos_version;
alter table person rename column creator_agreement_level  to creator_agreement_version;

-- jdevos 9/11/2013
alter table person rename column creator_agreement_version  to contributor_agreement_version;

-- TDAR-1978 mpaulo 9/23/2013
alter table latitude_longitude add column is_ok_to_show_exact_location boolean default false;

-- TDAR-1978 mpaulo 9/25/2013
alter table latitude_longitude add column min_obfuscated_lat double precision;
alter table latitude_longitude add column min_obfuscated_long double precision;
alter table latitude_longitude add column max_obfuscated_lat double precision;
alter table latitude_longitude add column max_obfuscated_long double precision;

-- jdevos 10/10/2013: this FK gets generated by hbm2ddl but is missing from prod/alpha and sample db.
alter table authorized_user add foreign key(user_id) references person;

-- jdevos 10/13/2013: adding some indexes per TDAR-3403.
create index authorized_user_resource_collection_id_idx on authorized_user(resource_collection_id);
create index authorized_user_user_id_idx on authorized_user(user_id);
create index bookmarked_resource_person_id_idx on bookmarked_resource(person_id);
create index bookmarked_resource_resource_id_idx on bookmarked_resource(resource_id);
create index coding_rule_coding_sheet_id_idx on coding_rule(coding_sheet_id);
create index coding_rule_ontology_node_id_idx on coding_rule(ontology_node_id);
create index coding_sheet_default_ontology_id_idx on coding_sheet(default_ontology_id);
create index collection_owner_id_idx on collection(owner_id);
create index collection_parent_id_idx on collection(parent_id);
create index data_table_column_data_table_id_idx on data_table_column(data_table_id);
create index data_table_column_default_coding_sheet_id_idx on data_table_column(default_coding_sheet_id);
create index data_table_column_default_ontology_id_idx on data_table_column(default_ontology_id);
create index related_comparative_collection_resource_id_idx on related_comparative_collection(resource_id);
create index source_collection_resource_id_idx on source_collection(resource_id);

-- abrin 12/4/2013 -- adding foreign keys that should have been there
alter table sensory_data_image
    add constraint FK_l4o8gyxxc17q6w3g8ew9ivhlh
    foreign key (sensory_data_id)
    references sensory_data;

alter table sensory_data_scan
    add constraint FK_bbetp1cmjicvtydwd0hfepab1
    foreign key (sensory_data_id)
    references sensory_data;

-- abrin 12/31/2013
alter table person add column orcid_id varchar(50) default null;

-- abrin 1/15/2014
alter table collection add column description_admin text default null;

-- abrin 1/24/2014
create index res_uploader on resource (uploader_id);

-- abrin 2/9/2014
CREATE TABLE weekly_popular_resource_cache (
    id bigserial NOT NULL,
    label character varying(255) NOT NULL,
    level character varying(50) NOT NULL,
    resource_count bigint,
    resource_id bigint
);

alter table weekly_popular_resource_cache drop column label;
alter table weekly_popular_resource_cache drop column level;
alter table weekly_popular_resource_cache drop column resource_count;

-- abrin 2/14/2014
create table creator_view_statistics (
    id  bigserial not null,
    date_accessed timestamp,
    creator_id int8 references creator,
    primary key (id)
);
create index creator_view_stats_count_id on creator_view_statistics (creator_id, id);


create table resource_collection_view_statistics (
    id  bigserial not null,
    date_accessed timestamp,
    resource_collection_id int8 references collection,
    primary key (id)
);

create index resource_collection_view_stats_count_id on resource_collection_view_statistics (resource_collection_id, id);
