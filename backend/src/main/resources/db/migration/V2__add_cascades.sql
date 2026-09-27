-- V2__add_cascades.sql
-- Adds ON DELETE CASCADE to foreign keys required for safe user deletion while preserving marketplace integrity

-- Drop existing constraints
ALTER TABLE public.content DROP CONSTRAINT fk14eidkbpu6n090ymygps0k1w;
ALTER TABLE public.refresh_tokens DROP CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os;
ALTER TABLE public.content_likes DROP CONSTRAINT fk3phynxqdk3ogejthkee6m3tr4;
ALTER TABLE public.creator_profiles DROP CONSTRAINT fk48jx3726hqfmcyksfm6rysgw1;
ALTER TABLE public.comments DROP CONSTRAINT fk8omq0tc18jd43bu5tjh6jvraq;
ALTER TABLE public.user_profiles DROP CONSTRAINT fkjcad5nfve11khsnpwj1mv8frj;
ALTER TABLE public.follows DROP CONSTRAINT fkonkdkae2ngtx70jqhsh7ol6uq;
ALTER TABLE public.follows DROP CONSTRAINT fkqnkw0cwwh6572nyhvdjqlr163;
ALTER TABLE public.campaign_applications DROP CONSTRAINT fks2rbghsrb3mfm1g187avae5gx;
ALTER TABLE public.comments DROP CONSTRAINT fksh7j8kli8b5f243ia10onk1eu;
ALTER TABLE public.brand_profiles DROP CONSTRAINT fksj9au0kfjkqbmlqja9q47nilo;
ALTER TABLE public.notifications DROP CONSTRAINT fkt8ievafor22iuvg5sd4p7lhbk;
ALTER TABLE public.content_likes DROP CONSTRAINT fktq9ln30we2d1nbibcevn56cxi;

-- Recreate with ON DELETE CASCADE
ALTER TABLE public.content ADD CONSTRAINT fk14eidkbpu6n090ymygps0k1w FOREIGN KEY (creator_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.refresh_tokens ADD CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.content_likes ADD CONSTRAINT fk3phynxqdk3ogejthkee6m3tr4 FOREIGN KEY (content_id) REFERENCES public.content(id) ON DELETE CASCADE;
ALTER TABLE public.creator_profiles ADD CONSTRAINT fk48jx3726hqfmcyksfm6rysgw1 FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.comments ADD CONSTRAINT fk8omq0tc18jd43bu5tjh6jvraq FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.user_profiles ADD CONSTRAINT fkjcad5nfve11khsnpwj1mv8frj FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.follows ADD CONSTRAINT fkonkdkae2ngtx70jqhsh7ol6uq FOREIGN KEY (following_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.follows ADD CONSTRAINT fkqnkw0cwwh6572nyhvdjqlr163 FOREIGN KEY (follower_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.campaign_applications ADD CONSTRAINT fks2rbghsrb3mfm1g187avae5gx FOREIGN KEY (creator_user_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.comments ADD CONSTRAINT fksh7j8kli8b5f243ia10onk1eu FOREIGN KEY (content_id) REFERENCES public.content(id) ON DELETE CASCADE;
ALTER TABLE public.brand_profiles ADD CONSTRAINT fksj9au0kfjkqbmlqja9q47nilo FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.notifications ADD CONSTRAINT fkt8ievafor22iuvg5sd4p7lhbk FOREIGN KEY (recipient_user_id) REFERENCES public.users(id) ON DELETE CASCADE;
ALTER TABLE public.content_likes ADD CONSTRAINT fktq9ln30we2d1nbibcevn56cxi FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

-- Add new columns for Cloudinary tracking
ALTER TABLE public.content ADD COLUMN cloudinary_public_id VARCHAR(255);
ALTER TABLE public.users ADD COLUMN avatar_public_id VARCHAR(255);
