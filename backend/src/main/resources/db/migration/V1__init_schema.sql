--
-- PostgreSQL database dump
--


-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: brand_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.brand_profiles (
    id bigint NOT NULL,
    company_name character varying(255),
    created_at timestamp(6) without time zone,
    description text,
    industry character varying(255),
    logo_url character varying(255),
    updated_at timestamp(6) without time zone,
    website_url character varying(255),
    user_id bigint NOT NULL
);


--
-- Name: brand_profiles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.brand_profiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: brand_profiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.brand_profiles_id_seq OWNED BY public.brand_profiles.id;


--
-- Name: campaign_applications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.campaign_applications (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    message text,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    campaign_id bigint NOT NULL,
    creator_user_id bigint NOT NULL,
    CONSTRAINT campaign_applications_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'WITHDRAWN'::character varying])::text[])))
);


--
-- Name: campaign_applications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.campaign_applications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: campaign_applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.campaign_applications_id_seq OWNED BY public.campaign_applications.id;


--
-- Name: campaigns; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.campaigns (
    id bigint NOT NULL,
    application_deadline timestamp(6) without time zone,
    budget numeric(10,2),
    created_at timestamp(6) without time zone,
    description text NOT NULL,
    niche character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    brand_user_id bigint NOT NULL,
    CONSTRAINT campaigns_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'OPEN'::character varying, 'PAUSED'::character varying, 'CLOSED'::character varying, 'COMPLETED'::character varying])::text[])))
);


--
-- Name: campaigns_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.campaigns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: campaigns_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.campaigns_id_seq OWNED BY public.campaigns.id;


--
-- Name: collaborations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.collaborations (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    campaign_id bigint NOT NULL,
    creator_user_id bigint NOT NULL,
    originating_application_id bigint NOT NULL,
    CONSTRAINT collaborations_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: collaborations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.collaborations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: collaborations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.collaborations_id_seq OWNED BY public.collaborations.id;


--
-- Name: comments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.comments (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    text text NOT NULL,
    updated_at timestamp(6) without time zone,
    content_id bigint NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: comments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.comments_id_seq OWNED BY public.comments.id;


--
-- Name: content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.content (
    id bigint NOT NULL,
    caption text,
    content_type character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    duration_seconds integer,
    file_size bigint,
    media_url character varying(1024) NOT NULL,
    mime_type character varying(100),
    published_at timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    thumbnail_url character varying(1024),
    title character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    visibility character varying(255) NOT NULL,
    creator_id bigint NOT NULL,
    CONSTRAINT content_content_type_check CHECK (((content_type)::text = ANY ((ARRAY['IMAGE'::character varying, 'VIDEO'::character varying, 'REEL'::character varying])::text[]))),
    CONSTRAINT content_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying])::text[]))),
    CONSTRAINT content_visibility_check CHECK (((visibility)::text = ANY ((ARRAY['PUBLIC'::character varying, 'PRIVATE'::character varying])::text[])))
);


--
-- Name: content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: content_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.content_id_seq OWNED BY public.content.id;


--
-- Name: content_likes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.content_likes (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    content_id bigint NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: content_likes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.content_likes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: content_likes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.content_likes_id_seq OWNED BY public.content_likes.id;


--
-- Name: conversations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversations (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    collaboration_id bigint NOT NULL
);


--
-- Name: conversations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.conversations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: conversations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.conversations_id_seq OWNED BY public.conversations.id;


--
-- Name: creator_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.creator_profiles (
    id bigint NOT NULL,
    bio text,
    created_at timestamp(6) without time zone,
    engagement_rate double precision,
    follower_count integer,
    niche character varying(255),
    updated_at timestamp(6) without time zone,
    user_id bigint NOT NULL
);


--
-- Name: creator_profiles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.creator_profiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: creator_profiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.creator_profiles_id_seq OWNED BY public.creator_profiles.id;


--
-- Name: deliverables; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.deliverables (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    description text,
    feedback text,
    status character varying(255) NOT NULL,
    submission_url character varying(2048),
    title character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    collaboration_id bigint NOT NULL,
    CONSTRAINT deliverables_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SUBMITTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


--
-- Name: deliverables_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.deliverables_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: deliverables_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.deliverables_id_seq OWNED BY public.deliverables.id;


--
-- Name: follows; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.follows (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    follower_id bigint NOT NULL,
    following_id bigint NOT NULL
);


--
-- Name: follows_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.follows_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: follows_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.follows_id_seq OWNED BY public.follows.id;


--
-- Name: messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages (
    id bigint NOT NULL,
    content text NOT NULL,
    created_at timestamp(6) without time zone,
    read_at timestamp(6) without time zone,
    conversation_id bigint NOT NULL,
    sender_user_id bigint NOT NULL
);


--
-- Name: messages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.messages_id_seq OWNED BY public.messages.id;


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    is_read boolean NOT NULL,
    message text,
    reference_id bigint,
    reference_type character varying(255),
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    recipient_user_id bigint NOT NULL,
    CONSTRAINT notifications_reference_type_check CHECK (((reference_type)::text = ANY ((ARRAY['COLLABORATION'::character varying, 'CAMPAIGN'::character varying, 'CONTENT'::character varying, 'USER'::character varying])::text[]))),
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['MESSAGE'::character varying, 'APPLICATION_UPDATE'::character varying, 'DELIVERABLE_UPDATE'::character varying, 'SOCIAL'::character varying])::text[])))
);


--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notifications_id_seq OWNED BY public.notifications.id;


--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_tokens (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    expires_at timestamp(6) without time zone NOT NULL,
    revoked boolean NOT NULL,
    token_hash character varying(255) NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.refresh_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.refresh_tokens_id_seq OWNED BY public.refresh_tokens.id;


--
-- Name: user_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_profiles (
    id bigint NOT NULL,
    bio text,
    created_at timestamp(6) without time zone,
    first_name character varying(255),
    last_name character varying(255),
    location character varying(255),
    profile_image_url character varying(255),
    updated_at timestamp(6) without time zone,
    website_url character varying(255),
    user_id bigint NOT NULL
);


--
-- Name: user_profiles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_profiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_profiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_profiles_id_seq OWNED BY public.user_profiles.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    display_name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    username character varying(255) NOT NULL,
    avatar_url character varying(1024),
    bio text,
    follower_count integer DEFAULT 0 NOT NULL,
    following_count integer DEFAULT 0 NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'CREATOR'::character varying, 'BRAND'::character varying, 'ADMIN'::character varying])::text[])))
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: brand_profiles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.brand_profiles ALTER COLUMN id SET DEFAULT nextval('public.brand_profiles_id_seq'::regclass);


--
-- Name: campaign_applications id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campaign_applications ALTER COLUMN id SET DEFAULT nextval('public.campaign_applications_id_seq'::regclass);


--
-- Name: campaigns id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campaigns ALTER COLUMN id SET DEFAULT nextval('public.campaigns_id_seq'::regclass);


--
-- Name: collaborations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collaborations ALTER COLUMN id SET DEFAULT nextval('public.collaborations_id_seq'::regclass);


--
-- Name: comments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comments ALTER COLUMN id SET DEFAULT nextval('public.comments_id_seq'::regclass);


--
-- Name: content id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content ALTER COLUMN id SET DEFAULT nextval('public.content_id_seq'::regclass);


--
-- Name: content_likes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_likes ALTER COLUMN id SET DEFAULT nextval('public.content_likes_id_seq'::regclass);


--
-- Name: conversations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations ALTER COLUMN id SET DEFAULT nextval('public.conversations_id_seq'::regclass);


--
-- Name: creator_profiles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creator_profiles ALTER COLUMN id SET DEFAULT nextval('public.creator_profiles_id_seq'::regclass);


--
-- Name: deliverables id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deliverables ALTER COLUMN id SET DEFAULT nextval('public.deliverables_id_seq'::regclass);


--
-- Name: follows id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows ALTER COLUMN id SET DEFAULT nextval('public.follows_id_seq'::regclass);


--
-- Name: messages id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ALTER COLUMN id SET DEFAULT nextval('public.messages_id_seq'::regclass);


--
-- Name: notifications id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications ALTER COLUMN id SET DEFAULT nextval('public.notifications_id_seq'::regclass);


--
-- Name: refresh_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens ALTER COLUMN id SET DEFAULT nextval('public.refresh_tokens_id_seq'::regclass);


--
-- Name: user_profiles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profiles ALTER COLUMN id SET DEFAULT nextval('public.user_profiles_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: brand_profiles brand_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.brand_profiles
    ADD CONSTRAINT brand_profiles_pkey PRIMARY KEY (id);


--
-- Name: campaign_applications campaign_applications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campaign_applications
    ADD CONSTRAINT campaign_applications_pkey PRIMARY KEY (id);


--
-- Name: campaigns campaigns_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campaigns
    ADD CONSTRAINT campaigns_pkey PRIMARY KEY (id);


--
-- Name: collaborations collaborations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collaborations
    ADD CONSTRAINT collaborations_pkey PRIMARY KEY (id);


--
-- Name: comments comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);


--
-- Name: content_likes content_likes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_likes
    ADD CONSTRAINT content_likes_pkey PRIMARY KEY (id);


--
-- Name: content content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content
    ADD CONSTRAINT content_pkey PRIMARY KEY (id);


--
-- Name: conversations conversations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);


--
-- Name: creator_profiles creator_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creator_profiles
    ADD CONSTRAINT creator_profiles_pkey PRIMARY KEY (id);


--
-- Name: deliverables deliverables_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deliverables
    ADD CONSTRAINT deliverables_pkey PRIMARY KEY (id);


--
-- Name: follows follows_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows
    ADD CONSTRAINT follows_pkey PRIMARY KEY (id);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: content_likes uk2fdd43eluwci6hqdkk7nbsjcc; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_likes
    ADD CONSTRAINT uk2fdd43eluwci6hqdkk7nbsjcc UNIQUE (user_id, content_id);


--
-- Name: follows uk4faelgsm2rxl2jf3iyjy981ro; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows
    ADD CONSTRAINT uk4faelgsm2rxl2jf3iyjy981ro UNIQUE (follower_id, following_id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: user_profiles uk_e5h89rk3ijvdmaiig4srogdc6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT uk_e5h89rk3ijvdmaiig4srogdc6 UNIQUE (user_id);


--
-- Name: creator_profiles uk_gsos4mp0tlrht38c1wn24ehn6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creator_profiles
    ADD CONSTRAINT uk_gsos4mp0tlrht38c1wn24ehn6 UNIQUE (user_id);


--
-- Name: brand_profiles uk_ia41rgy64n1kt462fhjhvfmw6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.brand_profiles
    ADD CONSTRAINT uk_ia41rgy64n1kt462fhjhvfmw6 UNIQUE (user_id);


--
-- Name: refresh_tokens uk_o2mlirhldriil2y7krapq4frt; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT uk_o2mlirhldriil2y7krapq4frt UNIQUE (token_hash);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: conversations uk_say83uxu5uuga8k1p117mfi1f; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT uk_say83uxu5uuga8k1p117mfi1f UNIQUE (collaboration_id);


--
-- Name: campaign_applications ukctyygj6shc2wcf6d18stn3uqe; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campaign_applications
    ADD CONSTRAINT ukctyygj6shc2wcf6d18stn3uqe UNIQUE (campaign_id, creator_user_id);


--
-- Name: collaborations ukd0eu5kp4byxby50rc8u2671mt; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collaborations
    ADD CONSTRAINT ukd0eu5kp4byxby50rc8u2671mt UNIQUE (originating_application_id);


--
-- Name: user_profiles user_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_creator_profile_niche; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_creator_profile_niche ON public.creator_profiles USING btree (niche);


--
-- Name: idx_deliverable_collaboration_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_deliverable_collaboration_id ON public.deliverables USING btree (collaboration_id);


--
-- Name: idx_message_conversation_id_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_message_conversation_id_created_at ON public.messages USING btree (conversation_id, created_at);


--
-- Name: idx_notifications_recipient_unread; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_recipient_unread ON public.notifications USING btree (recipient_user_id, is_read, created_at);


--
-- Name: content fk14eidkbpu6n090ymygps0k1w; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content
    ADD CONSTRAINT fk14eidkbpu6n090ymygps0k1w FOREIGN KEY (creator_id) REFERENCES public.users(id);


--
-- Name: refresh_tokens fk1lih5y2npsf8u5o3vhdb9y0os; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: content_likes fk3phynxqdk3ogejthkee6m3tr4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_likes
    ADD CONSTRAINT fk3phynxqdk3ogejthkee6m3tr4 FOREIGN KEY (content_id) REFERENCES public.content(id);


--
-- Name: creator_profiles fk48jx3726hqfmcyksfm6rysgw1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creator_profiles
    ADD CONSTRAINT fk48jx3726hqfmcyksfm6rysgw1 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: deliverables fk8nsjpjm7drst0pkedkx132fho; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deliverables
    ADD CONSTRAINT fk8nsjpjm7drst0pkedkx132fho FOREIGN KEY (collaboration_id) REFERENCES public.collaborations(id);


--
-- Name: comments fk8omq0tc18jd43bu5tjh6jvraq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT fk8omq0tc18jd43bu5tjh6jvraq FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: collaborations fkbsqmhtd8w9af4cboq8j3nvytc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collaborations
    ADD CONSTRAINT fkbsqmhtd8w9af4cboq8j3nvytc FOREIGN KEY (campaign_id) REFERENCES public.campaigns(id);


--
-- Name: campaign_applications fkgh96i62y8l8269qmfli0d5u9v; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campaign_applications
    ADD CONSTRAINT fkgh96i62y8l8269qmfli0d5u9v FOREIGN KEY (campaign_id) REFERENCES public.campaigns(id);


--
-- Name: collaborations fkh0fwyagoree953j4fvc75rf0u; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collaborations
    ADD CONSTRAINT fkh0fwyagoree953j4fvc75rf0u FOREIGN KEY (originating_application_id) REFERENCES public.campaign_applications(id);


--
-- Name: campaigns fkifx4jh7f1n2ipltiut2qbulyh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campaigns
    ADD CONSTRAINT fkifx4jh7f1n2ipltiut2qbulyh FOREIGN KEY (brand_user_id) REFERENCES public.users(id);


--
-- Name: user_profiles fkjcad5nfve11khsnpwj1mv8frj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT fkjcad5nfve11khsnpwj1mv8frj FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: messages fkk4mpqp6gfuaelpcamqv01brkr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkk4mpqp6gfuaelpcamqv01brkr FOREIGN KEY (sender_user_id) REFERENCES public.users(id);


--
-- Name: collaborations fkl2e7fmt4dgi7eeu2pyj60g3hx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collaborations
    ADD CONSTRAINT fkl2e7fmt4dgi7eeu2pyj60g3hx FOREIGN KEY (creator_user_id) REFERENCES public.users(id);


--
-- Name: conversations fknc9lk0wfmi9wo8glptatn4jjl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT fknc9lk0wfmi9wo8glptatn4jjl FOREIGN KEY (collaboration_id) REFERENCES public.collaborations(id);


--
-- Name: follows fkonkdkae2ngtx70jqhsh7ol6uq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows
    ADD CONSTRAINT fkonkdkae2ngtx70jqhsh7ol6uq FOREIGN KEY (following_id) REFERENCES public.users(id);


--
-- Name: follows fkqnkw0cwwh6572nyhvdjqlr163; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows
    ADD CONSTRAINT fkqnkw0cwwh6572nyhvdjqlr163 FOREIGN KEY (follower_id) REFERENCES public.users(id);


--
-- Name: campaign_applications fks2rbghsrb3mfm1g187avae5gx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.campaign_applications
    ADD CONSTRAINT fks2rbghsrb3mfm1g187avae5gx FOREIGN KEY (creator_user_id) REFERENCES public.users(id);


--
-- Name: comments fksh7j8kli8b5f243ia10onk1eu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT fksh7j8kli8b5f243ia10onk1eu FOREIGN KEY (content_id) REFERENCES public.content(id);


--
-- Name: brand_profiles fksj9au0kfjkqbmlqja9q47nilo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.brand_profiles
    ADD CONSTRAINT fksj9au0kfjkqbmlqja9q47nilo FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: messages fkt492th6wsovh1nush5yl5jj8e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkt492th6wsovh1nush5yl5jj8e FOREIGN KEY (conversation_id) REFERENCES public.conversations(id);


--
-- Name: notifications fkt8ievafor22iuvg5sd4p7lhbk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fkt8ievafor22iuvg5sd4p7lhbk FOREIGN KEY (recipient_user_id) REFERENCES public.users(id);


--
-- Name: content_likes fktq9ln30we2d1nbibcevn56cxi; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.content_likes
    ADD CONSTRAINT fktq9ln30we2d1nbibcevn56cxi FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--


